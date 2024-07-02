package com.writenbite.bisonfun.api.client;

import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Setter
@AllArgsConstructor
public class ExceptionData<T>{
    private T data;

    public Optional<T> getData() {
        if(data == null){
            return Optional.empty();
        }else {
            return Optional.of(data);
        }
    }

}
