package com.writenbite.bisonfun.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.client.anilist.TooManyAnimeRequestsException;
import com.writenbite.bisonfun.api.database.entity.User;
import com.writenbite.bisonfun.api.security.UserNotFoundException;
import com.writenbite.bisonfun.api.service.UserService;
import com.writenbite.bisonfun.api.service.UserVideoContentService;
import com.writenbite.bisonfun.api.types.Connection;
import com.writenbite.bisonfun.api.types.uservideocontent.UserVideoContentListElement;
import com.writenbite.bisonfun.api.types.uservideocontent.input.UpdateUserVideoContentListElementInput;
import com.writenbite.bisonfun.api.types.uservideocontent.input.UserVideoContentListInput;
import com.writenbite.bisonfun.api.types.uservideocontent.output.UpdateUserVideoContentListElementPayload;
import com.writenbite.bisonfun.api.types.videocontent.VideoContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@Controller
public class UserVideoContentController {

    private final UserVideoContentService userVideoContentService;
    private final UserService userService;

    @Autowired
    public UserVideoContentController(UserVideoContentService userVideoContentService, UserService userService) {
        this.userVideoContentService = userVideoContentService;
        this.userService = userService;
    }

    @QueryMapping
    public Connection<UserVideoContentListElement> userVideoContentList(@Argument final int userId, @Argument final UserVideoContentListInput filter, @Argument final Integer page) {
        return userVideoContentService.userVideoContentList(userId, filter, page);
    }

    @QueryMapping
    public UserVideoContentListElement userVideoContent(@Argument final int userId, @Argument final long contentId) throws ContentNotFoundException {
        return userVideoContentService.userVideoContentListElement(userId, contentId);
    }

    @PreAuthorize("hasRole(T(com.writenbite.bisonfun.api.security.Role).ROLE_ACCESS)")
    @MutationMapping
    public UpdateUserVideoContentListElementPayload updateUserVideoContentListElement(@Argument final UpdateUserVideoContentListElementInput input, Principal principal) throws UserNotFoundException, ContentNotFoundException, TooManyAnimeRequestsException, JsonProcessingException {
        Optional<User> optionalUser = userService.getUserByUsername(principal.getName());
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(principal.getName());
        }
        return userVideoContentService.updateUserVideoContentListElement(optionalUser.get(), input);
    }

    @PreAuthorize("hasRole(T(com.writenbite.bisonfun.api.security.Role).ROLE_ACCESS)")
    @QueryMapping
    public VideoContent.BasicInfo whatToWatch(@Argument final UserVideoContentListInput input, Principal principal) throws UserNotFoundException {
        Optional<User> optionalUser = userService.getUserByUsername(principal.getName());
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(principal.getName());
        }
        return userVideoContentService.whatToWatch(optionalUser.get().getId(), input);
    }
}
