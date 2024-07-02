package com.writenbite.bisonfun.api.database.converter;

import com.writenbite.bisonfun.api.database.entity.VideoContentCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class VideoContentCategoryConverter implements AttributeConverter<VideoContentCategory, String> {
    @Override
    public String convertToDatabaseColumn(VideoContentCategory videoContentCategory) {
        if(videoContentCategory == null){
            return null;
        }
        return videoContentCategory.getString();
    }

    @Override
    public VideoContentCategory convertToEntityAttribute(String s) {
        if(s == null){
            return null;
        }
        return Stream.of(VideoContentCategory.values())
                .filter(c -> c.getString().equalsIgnoreCase(s))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
