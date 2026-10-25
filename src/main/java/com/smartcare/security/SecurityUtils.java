package com.smartcare.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Small helper so controllers (Phase 8) can pull the authenticated user's
 * id/role off the JWT-derived principal without re-querying the DB by email.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static UserPrincipal getCurrentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user found in the security context");
        }
        return principal;
    }

    public static Long getCurrentUserId() {
        return getCurrentPrincipal().getId();
    }

    public static String getCurrentUserEmail() {
        return getCurrentPrincipal().getUsername();
    }

    /** Returns the role including its "ROLE_" prefix, e.g. "ROLE_PATIENT". */
    public static String getCurrentUserRole() {
        return getCurrentPrincipal().getRole();
    }
}
