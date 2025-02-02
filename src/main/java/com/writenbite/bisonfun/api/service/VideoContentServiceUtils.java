package com.writenbite.bisonfun.api.service;

public class VideoContentServiceUtils {
    private VideoContentServiceUtils() {}

    public static boolean isNonEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isNotNullAndPositive(Number number){
        return number != null && number.intValue() > 0;
    }

    public static boolean isConflictingContent(String originalTitle, String sideTitle){
        return !originalTitle.equalsIgnoreCase(sideTitle);
    }
}
