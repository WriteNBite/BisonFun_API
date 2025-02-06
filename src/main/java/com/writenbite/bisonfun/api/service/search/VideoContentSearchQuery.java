package com.writenbite.bisonfun.api.service.search;

import com.writenbite.bisonfun.api.types.videocontent.VideoContentCategory;
import com.writenbite.bisonfun.api.types.videocontent.VideoContentFormat;

import java.util.List;

public record VideoContentSearchQuery(
        String query,
        VideoContentCategory category,
        List<VideoContentFormat> formats,
        Integer page
) {
}
