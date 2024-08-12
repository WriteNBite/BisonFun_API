package com.writenbite.bisonfun.api.controller.handler;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import com.writenbite.bisonfun.api.security.TokenExpiredException;
import com.writenbite.bisonfun.api.security.TokenValidationException;
import com.writenbite.bisonfun.api.security.UserNotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Component;

@Component
public class ExceptionHandler extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        GraphqlErrorBuilder<?> errorBuilder = GraphqlErrorBuilder.newError(env)
                .message(ex.getMessage() != null ? ex.getMessage() : "Unknown error");
        if (ex instanceof ContentNotFoundException || ex instanceof UserNotFoundException) {
            errorBuilder.errorType(ErrorType.NOT_FOUND);
        } else if (ex instanceof TokenValidationException || ex instanceof BadCredentialsException || ex instanceof IllegalArgumentException) {
            errorBuilder.errorType(ErrorType.BAD_REQUEST);
        } else if (ex instanceof TokenExpiredException || ex instanceof AuthenticationCredentialsNotFoundException || ex instanceof AuthorizationDeniedException) {
            errorBuilder.errorType(ErrorType.UNAUTHORIZED);
        } else {
            logger.error(ex.getMessage(), ex);
            errorBuilder
                    .message("Something went wrong")
                    .errorType(ErrorType.INTERNAL_ERROR);
        }
        return errorBuilder.build();
    }
}
