package com.writenbite.bisonfun.api.types.builder;

import com.writenbite.bisonfun.api.types.videocontent.*;

public class VideoContentBasicInfoBuilder {

    private Long id;
    private VideoContentTitle title;
    private String poster;
    private VideoContentCategory category;
    private VideoContentFormat videoContentFormat;
    private Integer year;
    private ExternalIdBuilder externalIdBuilder = new ExternalIdBuilder();

    public VideoContentBasicInfoBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public VideoContentBasicInfoBuilder title(VideoContentTitle title) {
        this.title = title;
        return this;
    }

    public VideoContentBasicInfoBuilder titleIfEmptyOrNull(VideoContentTitle title){
        if(this.title == null || title.english().isEmpty()){
            this.title = title;
        }
        return this;
    }

    public VideoContentBasicInfoBuilder poster(String poster) {
        this.poster = poster;
        return this;
    }

    public VideoContentBasicInfoBuilder posterIfEmptyOrNull(String poster){
        if(this.poster == null || this.poster.isEmpty()){
            this.poster = poster;
        }
        return this;
    }

    public VideoContentBasicInfoBuilder category(VideoContentCategory category) {
        this.category = category;
        return this;
    }

    public VideoContentBasicInfoBuilder categoryIfNull(VideoContentCategory category){
        if(this.category == null){
            this.category = category;
        }
        return this;
    }

    public VideoContentBasicInfoBuilder videoContentFormat(VideoContentFormat videoContentFormat) {
        this.videoContentFormat = videoContentFormat;
        return this;
    }

    public VideoContentBasicInfoBuilder videoContentFormatIfNull(VideoContentFormat videoContentFormat){
        if(this.videoContentFormat == null){
            this.videoContentFormat = videoContentFormat;
        }
        return this;
    }

    public VideoContentBasicInfoBuilder year(Integer year) {
        this.year = year;
        return this;
    }

    public VideoContentBasicInfoBuilder yearIfEmptyOrNull(Integer year){
        if(this.year == null || this.year <= 0){
            this.year = year;
        }
        return this;
    }

    public ExternalIdBuilder getExternalId() {
        return this.externalIdBuilder;
    }

    public VideoContentBasicInfoBuilder setExternalId(ExternalId externalId){
        this.externalIdBuilder = new ExternalIdBuilder(externalId);
        return this;
    }

    public VideoContent.BasicInfo build() {
        return new VideoContent.BasicInfo(id, title, poster, category, videoContentFormat, year, externalIdBuilder.build());
    }
}
