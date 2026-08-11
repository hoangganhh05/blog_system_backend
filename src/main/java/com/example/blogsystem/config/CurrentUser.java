package com.example.blogsystem.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Supplies the identity established by the verified JWT; never trust a userId from a request. */
@Component
public class CurrentUser {
    public Long id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required");
        }
        try {
            return Long.valueOf(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid authentication context");
        }
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public void requireOwnerOrAdmin(Long ownerId) {
        if (!isAdmin() && !id().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to modify this resource");
        }
    }
}
