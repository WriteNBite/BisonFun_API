package com.writenbite.bisonfun.api.service.external.tmdb;

import com.writenbite.bisonfun.api.database.entity.VideoContentType;

public record TmdbVideoContentIdInput(Integer tmdbId, VideoContentType type) {
}
