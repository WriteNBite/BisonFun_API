package com.writenbite.bisonfun.api.security;

public class TokenExpiredException extends Exception {

    public TokenExpiredException() {
        super("Token is expired");
    }
}
