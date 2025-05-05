package org.treasurehunt.common.util;

import org.springframework.security.core.context.SecurityContextHolder;
import org.treasurehunt.security.UserDetailsDTO;

import java.util.Optional;

public final class AuthUtil {

    private AuthUtil(){

    }

    public static Optional<UserDetailsDTO> getUserFromSecurityContext() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getPrincipal() instanceof UserDetailsDTO user ? Optional.of(user) : Optional.empty();
    }
}
