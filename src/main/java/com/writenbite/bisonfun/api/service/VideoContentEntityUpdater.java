package com.writenbite.bisonfun.api.service;

import com.writenbite.bisonfun.api.database.entity.VideoContent;
import com.writenbite.bisonfun.api.database.entity.VideoContentCategory;

import static com.writenbite.bisonfun.api.database.entity.VideoContentType.UNKNOWN;
import static com.writenbite.bisonfun.api.service.VideoContentServiceUtils.isNonEmpty;

public class VideoContentEntityUpdater {
    private VideoContentEntityUpdater() {}

    /**
     * @param existing video content from database that need to update
     * @param updated video content from external sources
     * @return <code>true</code> if poster was updated
     * @see VideoContentServiceUtils#isNonEmpty(String)
     */
    public static boolean updatePoster(VideoContent existing, VideoContent updated) {
        if (isNonEmpty(updated.getPoster()) && (isNonEmpty(existing.getPoster()) || !existing.getPoster().equals(updated.getPoster()))) {
            existing.setPoster(updated.getPoster());
            return true;
        }
        return false;
    }

    /**
     * @param existing video content from database that need to update
     * @param updated video content from external sources
     * @return <code>true</code> if category was updated
     */
    public static boolean updateCategory(VideoContent existing, VideoContent updated) {
        if (existing.getCategory() == VideoContentCategory.MAINSTREAM && updated.getCategory() != VideoContentCategory.MAINSTREAM) {
            existing.setCategory(updated.getCategory());
            return true;
        }
        return false;
    }

    /**
     * @param existing video content from database that need to update
     * @param updated video content from external sources
     * @return <code>true</code> if type was updated
     */
    public static boolean updateType(VideoContent existing, VideoContent updated) {
        if (existing.getType() == UNKNOWN && updated.getType() != UNKNOWN) {
            existing.setType(updated.getType());
            return true;
        }
        return false;
    }

    /**
     * @param existing video content from database that need to update
     * @param updated video content from external sources
     * @return <code>true</code> if year was updated
     */
    public static boolean updateYear(VideoContent existing, VideoContent updated) {
        boolean sameYear = existing.getYear().equals(updated.getYear());
        if (updated.getYear() > 0 && !sameYear) {
            existing.setYear(updated.getYear());
            return true;
        }
        return false;
    }
}
