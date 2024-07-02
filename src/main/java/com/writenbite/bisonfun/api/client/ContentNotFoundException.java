package com.writenbite.bisonfun.api.client;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Content not found")
public class ContentNotFoundException extends Exception{
    public ContentNotFoundException() {
        super("Content not found");
    }

    public ContentNotFoundException(String message) {
        super(message);
    }
}
