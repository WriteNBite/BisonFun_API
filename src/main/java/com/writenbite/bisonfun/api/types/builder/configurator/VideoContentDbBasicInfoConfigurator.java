package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.mapper.VideoContentMapper;
import com.writenbite.bisonfun.api.types.builder.VideoContentBasicInfoBuilder;
import org.springframework.stereotype.Component;

@Component
public class VideoContentDbBasicInfoConfigurator extends VideoContentBasicInfoBuilderConfigurator<VideoContent> {
    private final VideoContentMapper videoContentDbMapper;
    private final static int PRIORITY = 0;

    public VideoContentDbBasicInfoConfigurator(VideoContentMapper videoContentDbMapper) {
        this.videoContentDbMapper = videoContentDbMapper;
    }

    @Override
    public void configure(VideoContentBasicInfoBuilder builder, VideoContent videoContentDb) {
        if(videoContentDb != null) {
            com.writenbite.bisonfun.api.types.videocontent.VideoContent.BasicInfo databaseVideoContent = videoContentDbMapper.toBasicInfo(videoContentDb);

            builder.id(databaseVideoContent.id())
                    .title(databaseVideoContent.title())
                    .poster(databaseVideoContent.poster())
                    .year(databaseVideoContent.year())
                    .category(databaseVideoContent.category())
                    .videoContentFormat(databaseVideoContent.format());
            builder.getExternalId().updateFrom(databaseVideoContent.externalIds());
        }
    }

    @Override
    public int getPriority() {
        return PRIORITY;
    }
}
