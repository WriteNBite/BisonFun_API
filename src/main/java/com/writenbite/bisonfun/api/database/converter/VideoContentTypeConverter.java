package com.writenbite.bisonfun.api.database.converter;

import com.writenbite.bisonfun.api.database.entity.VideoContentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class VideoContentTypeConverter implements AttributeConverter<VideoContentType, String> {
    @Override
    public String convertToDatabaseColumn(VideoContentType videoContentType) {
        if(videoContentType == null){
            return null;
        }
        return videoContentType.getString();
    }

    @Override
    public VideoContentType convertToEntityAttribute(String s) {
        if(s == null){
            return null;
        }
        return Stream.of(VideoContentType.values())
                .filter(c -> c.getString().equals(s))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
