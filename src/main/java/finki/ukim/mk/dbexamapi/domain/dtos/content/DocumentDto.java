package finki.ukim.mk.dbexamapi.domain.dtos.content;

import finki.ukim.mk.dbexamapi.domain.models.content.Folder;

public record DocumentDto(
        Folder folder,
        String name,
        String contentMd,
        String filePath,
        String url,
        String mimeType,
        Integer sizeBytes,
        int sortOrder
) {
}
