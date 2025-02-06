package com.writenbite.bisonfun.api.types.builder.configurator;

import com.writenbite.bisonfun.api.service.RawVideoContent;
import com.writenbite.bisonfun.api.types.builder.VideoContentBasicInfoBuilder;

public abstract class VideoContentBasicInfoBuilderConfigurator<T extends RawVideoContent> implements Comparable<VideoContentBasicInfoBuilderConfigurator<?>> {

    public abstract void configure(VideoContentBasicInfoBuilder builder, T t);

    public abstract int getPriority();

    @Override
    public int compareTo(VideoContentBasicInfoBuilderConfigurator<?> o) {
        return Integer.compare(this.getPriority(), o.getPriority());
    }
}
