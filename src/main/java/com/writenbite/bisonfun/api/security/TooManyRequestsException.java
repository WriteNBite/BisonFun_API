package com.writenbite.bisonfun.api.security;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
@Getter
public class TooManyRequestsException extends RuntimeException {
    private final long seconds;

    public TooManyRequestsException(long seconds) {
        this("Too many requests. Will be available after " + seconds + " seconds", seconds);
    }

    public TooManyRequestsException(String errorMessage, long seconds) {
        super(errorMessage);
        this.seconds = seconds;
    }
}
