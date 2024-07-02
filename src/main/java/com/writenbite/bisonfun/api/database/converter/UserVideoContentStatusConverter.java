package com.writenbite.bisonfun.api.database.converter;

import com.writenbite.bisonfun.api.database.entity.UserVideoContentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class UserVideoContentStatusConverter implements AttributeConverter<UserVideoContentStatus, String> {
    @Override
    public String convertToDatabaseColumn(UserVideoContentStatus userVideoContentStatus) {
        if(userVideoContentStatus == null) {
            return null;
        }
        return userVideoContentStatus.getString();
    }

    @Override
    public UserVideoContentStatus convertToEntityAttribute(String s) {
        if(s == null) {
            return null;
        }
        return Stream.of(UserVideoContentStatus.values())
                .filter(c -> c.getString().equals(s))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
