package com.writenbite.bisonfun.api.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateVideoContentResponse<T> {
    private T data;
    private boolean changesMade;

    public UpdateVideoContentResponse   (T data, boolean changesMade) {
        this.data = data;
        this.changesMade = changesMade;
    }
}
