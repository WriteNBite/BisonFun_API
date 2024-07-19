package com.writenbite.bisonfun.api.types.uservideocontent;

import com.writenbite.bisonfun.api.types.user.User;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;

public record UserVideoContentListElement(
        User user,
        VideoContent.BasicInfo videoContent,
        Integer episodes,
        Integer score,
        UserVideoContentListStatus status
) {
}
