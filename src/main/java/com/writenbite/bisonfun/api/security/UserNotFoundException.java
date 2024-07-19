package com.writenbite.bisonfun.api.security;

public class UserNotFoundException extends Exception {
    public UserNotFoundException(String username) {
        super("User " + username + " not found");
    }
}
