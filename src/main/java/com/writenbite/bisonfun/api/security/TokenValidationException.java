package com.writenbite.bisonfun.api.security;

public class TokenValidationException extends Exception {
    public TokenValidationException() {
        super("Token is invalid.");
    }
}
