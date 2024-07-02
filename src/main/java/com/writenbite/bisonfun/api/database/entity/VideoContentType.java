package com.writenbite.bisonfun.api.database.entity;

import lombok.Getter;

@Getter
public enum VideoContentType {
    TV("TV"),
    MOVIE("Movie"),
    SPECIAL("Special"),
    MUSIC("Music"),
    UNKNOWN("Unknown");

    private final String string;
    VideoContentType(String string){
        this.string = string;
    }
}
