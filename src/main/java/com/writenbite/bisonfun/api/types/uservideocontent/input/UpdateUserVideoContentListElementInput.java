package com.writenbite.bisonfun.api.types.uservideocontent.input;

import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListStatus;

public record UpdateUserVideoContentListElementInput(
        Integer userId,
        Integer videoContentId,
        Integer episodes,
        Integer score,
        UserVideoContentListStatus status
) {
}
