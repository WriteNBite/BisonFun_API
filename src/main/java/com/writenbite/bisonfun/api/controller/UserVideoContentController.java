package com.writenbite.bisonfun.api.controller;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.service.UserVideoContentService;
import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;
import com.writenbite.bisonfun.api.types.uservideocontent.input.UserVideoContentListInput;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class UserVideoContentController {

    private final UserVideoContentService userVideoContentService;

    @Autowired
    public UserVideoContentController(UserVideoContentService userVideoContentService) {
        this.userVideoContentService = userVideoContentService;
    }

    @QueryMapping
    public Connection<UserVideoContentListElement> userVideoContentList(@Argument final int userId, @Argument final UserVideoContentListInput filter, @Argument final Integer page){
        return userVideoContentService.userVideoContentList(userId, filter, page);
    }
    @QueryMapping
    public UserVideoContentListElement userVideoContent(@Argument final int userId, @Argument final long contentId) throws ContentNotFoundException {
        return userVideoContentService.userVideoContentListElement(userId, contentId);
    }

    @QueryMapping
    public VideoContent.BasicInfo whatToWatch(@Argument final int userId, @Argument final UserVideoContentListInput input){
        return userVideoContentService.whatToWatch(userId, input);
    }
}
