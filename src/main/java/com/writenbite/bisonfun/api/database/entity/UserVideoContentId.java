package com.writenbite.bisonfun.api.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class UserVideoContentId implements Serializable {
    @Serial
    private static final long serialVersionUID = 6474801495149547663L;
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "video_content_id", nullable = false)
    private Long videoContentId;

    public UserVideoContentId(Integer userId, Long videoContentId) {
        this.userId = userId;
        this.videoContentId = videoContentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserVideoContentId entity = (UserVideoContentId) o;
        return Objects.equals(this.videoContentId, entity.videoContentId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(videoContentId, userId);
    }

}