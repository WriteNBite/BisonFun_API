package com.writenbite.bisonfun.api.database.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "video_content")
public class VideoContent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "ani_list_id")
    private Integer aniListId;

    @Column(name = "category")
    private VideoContentCategory category;

    @Column(name = "imdb_id")
    private String imdbId;

    @Column(name = "mal_id")
    private Integer malId;

    @Column(name = "poster")
    private String poster;

    @Column(name = "title")
    private String title;

    @Column(name = "tmdb_id")
    private Integer tmdbId;

    @Column(name = "type")
    private VideoContentType type;

    @Column(name = "year")
    private Integer year;

    @OneToMany(mappedBy = "videoContent")
    private Set<UserVideoContent> userVideoContents = new LinkedHashSet<>();

    @Override
    public String toString() {
        return "VideoContent{" +
                "id=" + id +
                ", aniListId=" + aniListId +
                ", category=" + category +
                ", imdbId='" + imdbId + '\'' +
                ", malId=" + malId +
                ", poster='" + poster + '\'' +
                ", title='" + title + '\'' +
                ", tmdbId=" + tmdbId +
                ", type=" + type +
                ", year=" + year +
                '}';
    }
}