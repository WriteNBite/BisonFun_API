package com.writenbite.bisonfun.api.controller.handler;

import com.writenbite.bisonfun.api.client.ContentNotFoundException;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

@Component
public class ExceptionHandler extends DataFetcherExceptionResolverAdapter {
    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        GraphqlErrorBuilder<?> errorBuilder = GraphqlErrorBuilder.newError(env)
                .message(ex.getMessage());
        if (ex instanceof ContentNotFoundException){
            errorBuilder.errorType(ErrorType.NOT_FOUND);
        }else{
            errorBuilder.errorType(ErrorType.INTERNAL_ERROR);
        }
        return errorBuilder.build();
    }
}
