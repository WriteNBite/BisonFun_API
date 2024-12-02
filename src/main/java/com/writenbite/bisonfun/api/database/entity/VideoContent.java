package com.writenbite.bisonfun.api.database.entity;

import com.writenbite.bisonfun.api.client.tmdb.TmdbPosterConfiguration;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
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

    @ColumnDefault("'2000-01-01'")
    @Column(name = "last_updated", nullable = false)
    private LocalDate lastUpdated;

    @PreUpdate
    @PrePersist
    public void prePersist(){
        lastUpdated = LocalDate.now();
    }

    public String getPoster() {
        if (poster.startsWith("/") && category == VideoContentCategory.MAINSTREAM){
            return TmdbPosterConfiguration.DEFAULT.getUrl() + poster;
        }
        return poster;
    }

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