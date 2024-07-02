package com.writenbite.bisonfun.api.client;

public interface VideoContentModel {
    int getEpisodes();
    Status getStatus();

    enum Status{
        ONGOING,
        RELEASED,
        UPCOMING,
        RUMORED,
        PLANNED,
        CANCELED,
        PAUSED,
        UNKNOWN
    }
}
