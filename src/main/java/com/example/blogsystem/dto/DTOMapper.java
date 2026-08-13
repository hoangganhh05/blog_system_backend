package com.example.blogsystem.dto;

import com.example.blogsystem.entity.Bookmark;
import com.example.blogsystem.entity.Category;
import com.example.blogsystem.entity.Comment;
import com.example.blogsystem.entity.Post;
import com.example.blogsystem.entity.PostLike;
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
            dto.setCategory(toCategoryDTO(post.getCategory()));
        }
        
        if (post.getSharedPost() != null) {
            dto.setSharedPost(toPostDTO(post.getSharedPost()));
        }
        
        return dto;
    }
    
    public static UserPublicDTO toUserPublicDTO(User user) {
        if (user == null) return null;
        
        UserPublicDTO dto = new UserPublicDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setBio(user.getBio());
        dto.setAvatarColor(user.getAvatarColor());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setBannerUrl(user.getBannerUrl());
        dto.setEmailPrivacy(user.getEmailPrivacy());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        
        return dto;
    }
    
    public static PostDTO.CategoryDTO toCategoryDTO(Category category) {
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
        dto.setType(postLike.getType() != null ? postLike.getType() : "LIKE");

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
}
