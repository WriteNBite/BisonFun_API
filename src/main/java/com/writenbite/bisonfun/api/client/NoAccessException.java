package com.writenbite.bisonfun.api.client;

public class NoAccessException extends Exception{
    public NoAccessException(String errorMessage){
        super(errorMessage);
    }
}
