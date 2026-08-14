package com.example.blogsystem.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.blogsystem.config.CurrentUser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping({"/games/caro", "/api/v1/games/caro"})
public class CaroGameController {

    public static class CaroRoom {
        private String roomCode;
        private Long hostId;
        private String hostName;
        private Long guestId;
        private String guestName;
        private String[] board; // 9 cells
        private Long turnId;
        private String status; // WAITING, PLAYING, FINISHED
        private Long winnerId;
        private String winnerSymbol;

        public CaroRoom() {
            this.board = new String[9];
            Arrays.fill(this.board, "");
        }

        public String getRoomCode() { return roomCode; }
        public void setRoomCode(String roomCode) { this.roomCode = roomCode; }

        public Long getHostId() { return hostId; }
        public void setHostId(Long hostId) { this.hostId = hostId; }

        public String getHostName() { return hostName; }
        public void setHostName(String hostName) { this.hostName = hostName; }

        public Long getGuestId() { return guestId; }
        public void setGuestId(Long guestId) { this.guestId = guestId; }

        public String getGuestName() { return guestName; }
        public void setGuestName(String guestName) { this.guestName = guestName; }

        public String[] getBoard() { return board; }
        public void setBoard(String[] board) { this.board = board; }

        public Long getTurnId() { return turnId; }
        public void setTurnId(Long turnId) { this.turnId = turnId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Long getWinnerId() { return winnerId; }
        public void setWinnerId(Long winnerId) { this.winnerId = winnerId; }

        public String getWinnerSymbol() { return winnerSymbol; }
        public void setWinnerSymbol(String winnerSymbol) { this.winnerSymbol = winnerSymbol; }
    }

    private final Map<String, CaroRoom> rooms = new ConcurrentHashMap<>();
    private final CurrentUser currentUser;

    public CaroGameController(CurrentUser currentUser) {
        this.currentUser = currentUser;
    }

    // 1. Tạo phòng mới: POST /games/caro/create?userId=...&userName=...
    @PostMapping("/create")
    public ResponseEntity<CaroRoom> createRoom(@RequestParam String userName) {
        Long userId = currentUser.id();
        String code = String.valueOf(1000 + new Random().nextInt(9000));
        CaroRoom room = new CaroRoom();
        room.setRoomCode(code);
        room.setHostId(userId);
        room.setHostName(userName);
        room.setTurnId(userId);
        room.setStatus("WAITING");

        rooms.put(code, room);
        return ResponseEntity.ok(room);
    }

    // 2. Tham gia phòng: POST /games/caro/join?roomCode=...&userId=...&userName=...
    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestParam String roomCode, @RequestParam String userName) {
        Long userId = currentUser.id();
        CaroRoom room = rooms.get(roomCode);
        if (room == null) {
            return ResponseEntity.badRequest().body("Phòng không tồn tại!");
        }

        if (room.getHostId().equals(userId)) {
            return ResponseEntity.ok(room);
        }

        if (room.getGuestId() != null && !room.getGuestId().equals(userId)) {
            return ResponseEntity.badRequest().body("Phòng đã đủ 2 người chơi!");
        }

        room.setGuestId(userId);
        room.setGuestName(userName);
        room.setStatus("PLAYING");

        return ResponseEntity.ok(room);
    }

    // 3. Đặt cờ: POST /games/caro/move?roomCode=...&userId=...&cellIndex=...
    @PostMapping("/move")
    public ResponseEntity<?> makeMove(@RequestParam String roomCode, @RequestParam int cellIndex) {
        Long userId = currentUser.id();
        CaroRoom room = rooms.get(roomCode);
        if (room == null) return ResponseEntity.badRequest().body("Phòng không tồn tại!");

        if (!"PLAYING".equals(room.getStatus())) {
            return ResponseEntity.badRequest().body("Trận đấu chưa bắt đầu hoặc đã kết thúc!");
        }

        if (!room.getTurnId().equals(userId)) {
            return ResponseEntity.badRequest().body("Chưa đến lượt của bạn!");
        }

        if (cellIndex < 0 || cellIndex >= 9 || !room.getBoard()[cellIndex].isEmpty()) {
            return ResponseEntity.badRequest().body("Ô này đã được đánh!");
        }

        String symbol = userId.equals(room.getHostId()) ? "X" : "O";
        room.getBoard()[cellIndex] = symbol;

        // Kiểm tra thắng
        if (checkWin(room.getBoard(), symbol)) {
            room.setStatus("FINISHED");
            room.setWinnerId(userId);
            room.setWinnerSymbol(symbol);
        } else if (isBoardFull(room.getBoard())) {
            room.setStatus("FINISHED");
            room.setWinnerSymbol("TIE");
        } else {
            // Đổi lượt
            Long nextTurn = userId.equals(room.getHostId()) ? room.getGuestId() : room.getHostId();
            room.setTurnId(nextTurn);
        }

        return ResponseEntity.ok(room);
    }

    // 4. Lấy thông tin phòng (Polling status): GET /games/caro/room/{roomCode}
    @GetMapping("/room/{roomCode}")
    public ResponseEntity<CaroRoom> getRoomInfo(@PathVariable String roomCode) {
        CaroRoom room = rooms.get(roomCode);
        if (room == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(room);
    }

    // 5. Chơi lại (Reset board): POST /games/caro/restart?roomCode=...
    @PostMapping("/restart")
    public ResponseEntity<?> restartGame(@RequestParam String roomCode) {
        CaroRoom room = rooms.get(roomCode);
        if (room == null) return ResponseEntity.notFound().build();
        if (!currentUser.id().equals(room.getHostId()) && !currentUser.id().equals(room.getGuestId())) {
            return ResponseEntity.status(403).build();
        }
        if (room != null) {
            Arrays.fill(room.getBoard(), "");
            room.setStatus(room.getGuestId() != null ? "PLAYING" : "WAITING");
            room.setWinnerId(null);
            room.setWinnerSymbol(null);
            room.setTurnId(room.getHostId());
        }
        return ResponseEntity.ok(room);
    }

    // 6. Lấy danh sách các phòng đang chờ: GET /games/caro/open-rooms
    @GetMapping("/open-rooms")
    public ResponseEntity<List<CaroRoom>> getOpenRooms() {
        List<CaroRoom> openList = new ArrayList<>();
        for (CaroRoom r : rooms.values()) {
            if ("WAITING".equals(r.getStatus())) {
                openList.add(r);
            }
        }
        return ResponseEntity.ok(openList);
    }

    private boolean checkWin(String[] b, String s) {
        int[][] lines = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
                {0, 4, 8}, {2, 4, 6}
        };
        for (int[] l : lines) {
            if (s.equals(b[l[0]]) && s.equals(b[l[1]]) && s.equals(b[l[2]])) {
                return true;
            }
        }
        return false;
    }

    private boolean isBoardFull(String[] b) {
        for (String cell : b) {
            if (cell == null || cell.isEmpty()) return false;
        }
        return true;
    }
}
