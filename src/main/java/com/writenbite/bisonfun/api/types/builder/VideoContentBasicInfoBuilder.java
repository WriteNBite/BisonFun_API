package com.writenbite.bisonfun.api.types.builder;

import com.writenbite.bisonfun.api.types.videocontent.*;

public class VideoContentBasicInfoBuilder {

    private Long id;
    private VideoContentTitle title;
    private String poster;
    private VideoContentCategory category;
    private VideoContentFormat videoContentFormat;
    private Integer year;
    private final ExternalIdBuilder externalIdBuilder;

    public VideoContentBasicInfoBuilder(){
        this.externalIdBuilder = new ExternalIdBuilder();
    }

    // Regular setters - always update
    public VideoContentBasicInfoBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public VideoContentBasicInfoBuilder title(VideoContentTitle title) {
        this.title = title;
        return this;
    }

    public VideoContentBasicInfoBuilder poster(String poster) {
        this.poster = poster;
        return this;
    }

    public VideoContentBasicInfoBuilder category(VideoContentCategory category) {
        this.category = category;
        return this;
    }

    public VideoContentBasicInfoBuilder videoContentFormat(VideoContentFormat videoContentFormat) {
        this.videoContentFormat = videoContentFormat;
        return this;
    }

    public VideoContentBasicInfoBuilder year(Integer year) {
        this.year = year;
        return this;
    }

    // Conditional setters - only update if current value is empty/null
    public VideoContentBasicInfoBuilder titleIfEmpty(VideoContentTitle title){
        if (isEmptyTitle(this.title) && !isEmptyTitle(title)) {
            this.title = title;
        }
        return this;
    }

    public VideoContentBasicInfoBuilder posterIfEmpty(String poster){
        if(isEmpty(this.poster) && !isEmpty(poster)){
            this.poster = poster;
        }
        return this;
    }

    public VideoContentBasicInfoBuilder categoryIfEmpty(VideoContentCategory category){
        if(this.category == null){
            this.category = category;
        }
        return this;
    }

    public VideoContentBasicInfoBuilder videoContentFormatIfEmpty(VideoContentFormat videoContentFormat){
        if(this.videoContentFormat == null){
            this.videoContentFormat = videoContentFormat;
        }
        return this;
    }

    public VideoContentBasicInfoBuilder yearIfEmpty(Integer year){
        if(isInvalidYear(this.year) & !isInvalidYear(year)){
            this.year = year;
        }
        return this;
    }

    // ExternalId access
    public ExternalIdBuilder getExternalId() {
        return this.externalIdBuilder;
    }

    // Build method
    public VideoContent.BasicInfo build() {
        validate();
        return new VideoContent.BasicInfo(
                id,
                title,
                poster,
                category,
                videoContentFormat,
                year,
                externalIdBuilder.build()
        );
    }

    //Validation helpers
    private boolean isEmptyTitle(VideoContentTitle title) {
        return title == null || title.english() == null || title.english().isEmpty();
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private boolean isInvalidYear(Integer year) {
        return year == null || year <= 0;
    }

    private void validate() {
        if (isEmptyTitle(title)) {
            throw new IllegalStateException("Title cannot be null or empty");
        }
        if (category == null) {
            throw new IllegalStateException("Category cannot be null");
        }
    }
}
