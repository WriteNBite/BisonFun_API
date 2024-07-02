package com.writenbite.bisonfun.api.database.entity;

import lombok.Getter;

@Getter
public enum VideoContentCategory {
    MAINSTREAM("Mainstream media"),
    ANIME("Anime");

    private final String string;
    VideoContentCategory(String string) {
        this.string = string;
    }
}
