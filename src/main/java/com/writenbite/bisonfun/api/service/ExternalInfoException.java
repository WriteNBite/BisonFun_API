package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.client.ExceptionDataHolder;

public class ExternalInfoException extends ExceptionDataHolder {
    public ExternalInfoException() {
        this("External Info is missing");
    }

    public <T> ExternalInfoException(T data){
        this("External Info is missing", data);
    }

    public ExternalInfoException(String message) {
        super(message);
    }

    public <T> ExternalInfoException(String message, T data) {
        super(message, data);
    }
}
