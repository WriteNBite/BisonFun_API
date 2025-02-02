package com.writenbite.bisonfun.api.types.videocontent.input;

import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;

public record TmdbVideoContentIdInput(Integer tmdbId, VideoContentFormat format) {
}
