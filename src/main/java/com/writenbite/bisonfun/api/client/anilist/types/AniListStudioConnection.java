package com.writenbite.bisonfun.api.client.anilist.types;

import java.io.Serializable;
import java.util.List;

public record AniListStudioConnection(
        List<AniListStudio> nodes
) implements Serializable {
}
