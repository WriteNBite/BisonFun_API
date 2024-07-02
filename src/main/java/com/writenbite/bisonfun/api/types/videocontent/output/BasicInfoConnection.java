package com.writenbite.bisonfun.api.types.videocontent.output;

import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;

import java.util.List;

public record BasicInfoConnection(
        List<VideoContent.BasicInfo> nodes,
        PageInfo pageInfo
) implements Connection<VideoContent.BasicInfo> {
}
