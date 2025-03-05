package org.treasurehunt.security.exception;

import org.springframework.security.core.AuthenticationException;

public class TokenAuthenticationException extends AuthenticationException {
    public TokenAuthenticationException(String msg) {
        super(msg);
    }

    public TokenAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}