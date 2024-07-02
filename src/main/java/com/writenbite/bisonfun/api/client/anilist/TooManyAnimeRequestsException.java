package com.writenbite.bisonfun.api.client.anilist;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Setter
@Getter
@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS, reason = "Too many requests to Anilist.co")
public class TooManyAnimeRequestsException extends Exception {
    private int seconds;

    public TooManyAnimeRequestsException(int seconds){
        this("Too many requests to Anilist.co. will be available after" + seconds + "seconds", seconds);
    }

    public TooManyAnimeRequestsException(String errorMessage, int seconds){
        super(errorMessage);
        this.seconds = seconds;
    }

}
