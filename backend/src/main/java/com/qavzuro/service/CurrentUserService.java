package com.qavzuro.service;

import com.qavzuro.exception.AuthenticationFailedException;
import com.qavzuro.security.QavzuroUserPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public QavzuroUserPrincipal getPrincipal() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        if (!(principal instanceof QavzuroUserPrincipal p)) {
            throw new AuthenticationFailedException("No authenticated user in context.");
        }
        return p;
    }

    public String getUserId() {
        return getPrincipal().getUserId();
    }

    public boolean isAuthenticated() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof QavzuroUserPrincipal;
    }
}
