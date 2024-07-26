package com.writenbite.bisonfun.api.client.anilist.types;

import java.io.Serializable;

public record AniListFuzzyDate(
        Integer year,
        Integer month,
        Integer day
) implements Serializable {
}
