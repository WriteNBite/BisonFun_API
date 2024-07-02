package com.writenbite.bisonfun.api.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_video_content")
public class UserVideoContent {
    @EmbeddedId
    private UserVideoContentId id;

    @MapsId("videoContentId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_content_id", nullable = false)
    private VideoContent videoContent;

    @Column(name = "episodes")
    private Integer episodes;

    @Column(name = "score")
    private Integer score;

    @Column(name = "status")
    private UserVideoContentStatus status;

    @Formula("CASE status WHEN 'Planned' THEN 1 WHEN 'Watching' THEN 2 WHEN 'Paused' THEN 3 WHEN 'Dropped' THEN 4 WHEN 'Complete' THEN 5 else 6 end")
    private int statusStage;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserVideoContent that = (UserVideoContent) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}