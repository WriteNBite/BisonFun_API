package com.writenbite.bisonfun.api.types.uservideocontent.input;

import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListStatus;
import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;

public record UpdateUserVideoContentListElementInput(
        VideoContentIdInput videoContentIdInput,
        Integer episodes,
        Integer score,
        UserVideoContentListStatus status
) {
}
