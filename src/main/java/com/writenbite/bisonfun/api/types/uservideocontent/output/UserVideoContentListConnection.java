package com.writenbite.bisonfun.api.types.uservideocontent.output;

import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.PageInfo;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;

import java.util.List;

public record UserVideoContentListConnection(
        List<UserVideoContentListElement> nodes,
        PageInfo pageInfo
) implements Connection<UserVideoContentListElement> {
}
