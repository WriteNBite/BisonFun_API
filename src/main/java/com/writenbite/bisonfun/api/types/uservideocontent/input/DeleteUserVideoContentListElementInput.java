package com.writenbite.bisonfun.api.types.uservideocontent.input;

public record DeleteUserVideoContentListElementInput(
        Integer userId,
        Integer videoContentId
) {
}
