package com.writenbite.bisonfun.api.types.uservideocontent.input;

import com.writenbite.bisonfun.api.types.videocontent.input.VideoContentIdInput;

public record DeleteUserVideoContentListElementInput(
        VideoContentIdInput videoContentIdInput
) {
}
