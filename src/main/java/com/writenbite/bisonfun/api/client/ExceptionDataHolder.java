package com.writenbite.bisonfun.api.client;

import java.util.Optional;

public class ExceptionDataHolder extends Exception{
    private final ExceptionData<?> data;

    public ExceptionDataHolder(String message) {
        this(message, null);
    }

    public<T> ExceptionDataHolder(String message, T data){
        super(message);
        this.data = new ExceptionData<>(data);
    }

    public <T> Optional<T> getData(Class<T> type){
        if(data.getData().isPresent()){
            return Optional.of(type.cast(data.getData().get()));
        }else{
            return Optional.empty();
        }
    }
}
