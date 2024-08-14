package com.writenbite.bisonfun.api.types.videocontent.input;

import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;

public record TmdbIdInput(Integer tmdbId, VideoContentFormat format) {
}
