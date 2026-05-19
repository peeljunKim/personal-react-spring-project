package org.personal.project.util;

import org.springframework.util.StringUtils;

/**
 * 이미지 파일명 규칙을 다루는 유틸리티입니다.
 * <p>
 * 실제 파일을 읽거나 저장하지 않고, DB에 저장된 원본 파일명을 기반으로
 * 썸네일 파일명으로 가져옵니다.
 */
public final class ImageFileNameUtil {

    private static final String THUMBNAIL_PREFIX = "s_";

    private ImageFileNameUtil() {
    }

    public static String toThumbnailFileName(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return null;
        }
        if (fileName.startsWith(THUMBNAIL_PREFIX)) {
            return fileName;
        }
        return THUMBNAIL_PREFIX + fileName;
    }
}
