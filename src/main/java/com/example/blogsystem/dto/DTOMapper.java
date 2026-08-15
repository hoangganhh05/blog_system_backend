package com.example.blogsystem.dto;

import com.example.blogsystem.entity.Bookmark;
import com.example.blogsystem.entity.Category;
import com.example.blogsystem.entity.ChatMessage;
import com.example.blogsystem.entity.Comment;
import com.example.blogsystem.entity.Friendship;
import com.example.blogsystem.entity.Notification;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.PostLike;
import com.example.blogsystem.entity.Story;
import com.example.blogsystem.entity.StoryView;
import com.example.blogsystem.entity.User;

public class DTOMapper {

    public static PostDTO toPostDTO(Post post) {
        if (post == null) return null;

        PostDTO dto = new PostDTO();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setThumbNail(post.getThumbNail());
        dto.setStatus(post.getStatus());
        dto.setBgColor(post.getBgColor());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setUpdatedAt(post.getUpdatedAt());
        dto.setViewCount(post.getViewCount());

        if (post.getUser() != null) {
            dto.setUser(toUserPublicDTO(post.getUser()));
        }

        if (post.getCategory() != null) {
            dto.setCategory(toPostCategoryDTO(post.getCategory()));
        }

        if (post.getSharedPost() != null) {
            dto.setSharedPost(toPostDTO(post.getSharedPost()));
        }

        return dto;
    }

    public static UserPublicDTO toUserPublicDTO(User user) {
        return toUserPublicDTO(user, true);
    }

    public static UserPublicDTO toUserPublicDTO(User user, boolean hasMessaged) {
        if (user == null) return null;

        UserPublicDTO dto = new UserPublicDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setBio(user.getBio());
        dto.setAvatarColor(user.getAvatarColor());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBannerUrl(user.getBannerUrl());

        boolean showStatus = !Boolean.FALSE.equals(user.getShowActiveStatus()) && hasMessaged;
        boolean isRecentActive = showStatus && (Boolean.TRUE.equals(user.getIsOnline()) ||
                (user.getLastActiveAt() != null && user.getLastActiveAt().isAfter(java.time.LocalDateTime.now().minusMinutes(3))));
        dto.setIsOnline(isRecentActive);
        dto.setLastActiveAt(showStatus ? user.getLastActiveAt() : null);
        dto.setShowActiveStatus(showStatus);

        return dto;
    }

    public static UserProfileDTO toUserProfileDTO(User user) {
        if (user == null) return null;

        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setBio(user.getBio());
        dto.setAvatarColor(user.getAvatarColor());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBannerUrl(user.getBannerUrl());
        dto.setEmailPrivacy(user.getEmailPrivacy());
        dto.setPostVisibility(user.getPostVisibility());
        dto.setFriendRequestScope(user.getFriendRequestScope());
        dto.setMessageScope(user.getMessageScope());
        dto.setShowActiveStatus(user.getShowActiveStatus());
        dto.setShowFollowingList(user.getShowFollowingList());
        dto.setShowFriendsList(user.getShowFriendsList());
        dto.setFriendListPrivacy(user.getFriendListPrivacy());
        dto.setFollowerListPrivacy(user.getFollowerListPrivacy());

        boolean isRecentActive = Boolean.TRUE.equals(user.getIsOnline()) ||
                (user.getLastActiveAt() != null && user.getLastActiveAt().isAfter(java.time.LocalDateTime.now().minusMinutes(5)));
        dto.setIsOnline(isRecentActive);
        dto.setLastActiveAt(user.getLastActiveAt());

        return dto;
    }

    public static CategoryDTO toCategoryDTO(Category category) {
        if (category == null) return null;

        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        return dto;
    }

    public static PostDTO.CategoryDTO toPostCategoryDTO(Category category) {
        if (category == null) return null;

        PostDTO.CategoryDTO dto = new PostDTO.CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());

        return dto;
    }

    public static BookmarkDTO toBookmarkDTO(Bookmark bookmark) {
        if (bookmark == null) return null;

        BookmarkDTO dto = new BookmarkDTO();
        dto.setId(bookmark.getId());
        dto.setCreatedAt(bookmark.getCreatedAt());

        if (bookmark.getUser() != null) {
            dto.setUser(toUserPublicDTO(bookmark.getUser()));
        }

        if (bookmark.getPost() != null) {
            dto.setPost(toPostDTO(bookmark.getPost()));
        }

        return dto;
    }

    public static UserReactionDTO toUserReactionDTO(PostLike postLike) {
        if (postLike == null) return null;

        UserReactionDTO dto = new UserReactionDTO();
        dto.setId(postLike.getId());
        dto.setType("LIKE");

        if (postLike.getUser() != null) {
            dto.setUserId(postLike.getUser().getId());
            dto.setUsername(postLike.getUser().getUsername());
            dto.setFullName(postLike.getUser().getFullName());
            dto.setAvatarUrl(postLike.getUser().getAvatarUrl());
            dto.setAvatarColor(postLike.getUser().getAvatarColor());
        }

        return dto;
    }

    public static CommentDTO toCommentDTO(Comment comment) {
        if (comment == null) return null;

        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        if (comment.getPost() != null) {
            dto.setPostId(comment.getPost().getId());
        }

        if (comment.getUser() != null) {
            dto.setUser(toUserPublicDTO(comment.getUser()));
        }

        return dto;
    }

    public static ChatMessageDTO toChatMessageDTO(ChatMessage message) {
        if (message == null) return null;

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());
        dto.setSender(toUserPublicDTO(message.getSender()));
        dto.setReceiver(toUserPublicDTO(message.getReceiver()));
        dto.setContent(message.getContent());
        dto.setRead(message.isRead());
        dto.setReadAt(message.getReadAt());
        dto.setCreatedAt(message.getCreatedAt());

        return dto;
    }

    public static FriendshipDTO toFriendshipDTO(Friendship friendship) {
        if (friendship == null) return null;

        FriendshipDTO dto = new FriendshipDTO();
        dto.setId(friendship.getId());
        dto.setRequester(toUserPublicDTO(friendship.getRequester()));
        dto.setReceiver(toUserPublicDTO(friendship.getReceiver()));
        dto.setStatus(friendship.getStatus());
        dto.setCreatedAt(friendship.getCreatedAt());

        return dto;
    }

    public static NotificationDTO toNotificationDTO(Notification notification) {
        if (notification == null) return null;

        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUser(toUserPublicDTO(notification.getUser()));
        dto.setSender(toUserPublicDTO(notification.getSender()));
        if (notification.getPost() != null) {
            dto.setPostId(notification.getPost().getId());
        }
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());

        return dto;
    }

    public static StoryDTO toStoryDTO(Story story) {
        if (story == null) return null;

        StoryDTO dto = new StoryDTO();
        dto.setId(story.getId());
        dto.setUser(toUserPublicDTO(story.getUser()));
        dto.setMediaUrl(story.getMediaUrl());
        dto.setTextContent(story.getTextContent());
        dto.setBgColor(story.getBgColor());
        dto.setCreatedAt(story.getCreatedAt());
        dto.setExpiresAt(story.getExpiresAt());
        dto.setIsArchived(story.getIsArchived());

        return dto;
    }

    public static StoryViewDTO toStoryViewDTO(StoryView view) {
        if (view == null) return null;

        StoryViewDTO dto = new StoryViewDTO();
        dto.setId(view.getId());
        if (view.getStory() != null) {
            dto.setStoryId(view.getStory().getId());
        }
        dto.setUser(toUserPublicDTO(view.getUser()));
        dto.setViewedAt(view.getViewedAt());
        dto.setReaction(view.getReaction());

        return dto;
    }
}
