package com.writenbite.bisonfun.api.types.videocontent.input;

public record VideoContentIdInput(
        Long videoContentId,
        Integer aniListId,
        TmdbVideoContentIdInput tmdbVideoContentIdInput
        ) {
}
