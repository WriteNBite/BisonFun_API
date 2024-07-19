package com.writenbite.bisonfun.api.security;

public class UserAlreadyExistException extends Exception {

    public UserAlreadyExistException(String username) {
        super("User " + username + " already exist");
    }
}
