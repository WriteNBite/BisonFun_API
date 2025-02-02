package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;
import org.springframework.stereotype.Component;

@Component
public class VideoContentInputValidator {

    public void validateVideoContentIdInput(VideoContentIdInput input) {
        if(input == null || allIdsNull(input)) {
            throw new IllegalArgumentException("At least one ID must be provided");
        }
    }

    private boolean allIdsNull(VideoContentIdInput input) {
        return !VideoContentServiceUtils.isNotNullAndPositive(input.videoContentId()) &&
                input.tmdbVideoContentIdInput() == null &&
                !VideoContentServiceUtils.isNotNullAndPositive(input.aniListId());
    }
}
