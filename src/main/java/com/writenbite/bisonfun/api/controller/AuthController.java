package com.writenbite.bisonfun.api.controller;

import com.writenbite.bisonfun.api.service.JwtService;
import com.writenbite.bisonfun.api.security.TokenType;
import com.writenbite.bisonfun.api.security.UserAlreadyExistException;
import com.writenbite.bisonfun.api.service.UserService;
import com.writenbite.bisonfun.api.types.auth.AccessTokenResponse;
import com.writenbite.bisonfun.api.types.auth.TokenPayload;
import com.writenbite.bisonfun.api.types.mapper.UserMapper;
import com.writenbite.bisonfun.api.types.user.RegisterInput;
import com.writenbite.bisonfun.api.types.user.User;
import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class AuthController {

    @Value("${bisonfun.jwt.time.access}")
    private long accessTokenExpirationTime;
    @Value("${bisonfun.jwt.time.refresh}")
    private long refreshTokenExpirationTime;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserMapper userMapper;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserService userService,
                          UserMapper userMapper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @MutationMapping
    public TokenPayload login(@Argument String username, @Argument String password) {
        Authentication auth;
        auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        SecurityContextHolder.getContext().setAuthentication(auth);
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        return generateTokenPayload(userDetails.getUsername());
    }

    private TokenPayload generateTokenPayload(String userName) {
        String accessToken = jwtService.generateToken(userName, accessTokenExpirationTime, TokenType.ACCESS);
        String refreshToken = jwtService.generateToken(userName, refreshTokenExpirationTime, TokenType.REFRESH);
        return new TokenPayload(accessToken, refreshToken);
    }

    @PreAuthorize("hasRole(T(com.writenbite.bisonfun.api.security.Role).ROLE_REFRESH)")
    @MutationMapping
    public AccessTokenResponse refreshAccessToken(Principal principal) {
        String accessToken = jwtService.generateToken(principal.getName(), accessTokenExpirationTime, TokenType.ACCESS);
        return new AccessTokenResponse(accessToken);
    }

    @PreAuthorize("hasRole(T(com.writenbite.bisonfun.api.security.Role).ROLE_REFRESH)")
    @MutationMapping
    public TokenPayload refreshToken(Principal principal) {
        return generateTokenPayload(principal.getName());
    }

    @MutationMapping
    public DataFetcherResult<User> register(@Argument RegisterInput input) {
        com.writenbite.bisonfun.api.database.entity.User user = new com.writenbite.bisonfun.api.database.entity.User();
        user.setUsername(input.username());
        user.setEmail(input.email());
        user.setPassword(input.password());
        DataFetcherResult.Builder<User> builder = DataFetcherResult.newResult();
        if (userService.existUserByUsernameOrEmail(user.getUsername(), user.getEmail())) {
            builder
                    .data(null)
                    .error(
                            GraphQLError.newError()
                                    .errorType(ErrorType.BAD_REQUEST)
                                    .message(new UserAlreadyExistException(user.getUsername()).getMessage())
                                    .build()
                    );
        } else {
            builder.data(userMapper.fromEntity(userService.saveUser(user)));
        }
        return builder.build();
    }
}
