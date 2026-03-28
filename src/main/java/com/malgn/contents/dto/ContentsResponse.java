package com.malgn.contents.dto;

import com.malgn.contents.entity.Contents;

import java.time.LocalDateTime;

public record ContentsResponse(
    Long id,
    String title,
    String description,
    Long viewCount,
    String createdBy,
    LocalDateTime createdDate,
    String lastModifiedBy,
    LocalDateTime lastModifiedDate
) {
    public static ContentsResponse from(Contents contents) {
        return new ContentsResponse(
            contents.getId(),
            contents.getTitle(),
            contents.getDescription(),
            contents.getViewCount(),
            contents.getCreatedBy(),
            contents.getCreatedDate(),
            contents.getLastModifiedBy(),
            contents.getLastModifiedDate()
        );
    }
}
