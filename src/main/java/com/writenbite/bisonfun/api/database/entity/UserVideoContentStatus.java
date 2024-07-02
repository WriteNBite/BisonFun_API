package com.writenbite.bisonfun.api.database.entity;

import lombok.Getter;

@Getter
public enum UserVideoContentStatus {
    PLANNED("Planned", 1),
    WATCHING("Watching", 2),
    PAUSED("Paused", 3),
    DROPPED("Dropped", 4),
    COMPLETE("Complete", 5);

    private final String string;
    private final int stage;
    UserVideoContentStatus(String string, int stage) {
        this.string = string;
        this.stage = stage;
    }
}
