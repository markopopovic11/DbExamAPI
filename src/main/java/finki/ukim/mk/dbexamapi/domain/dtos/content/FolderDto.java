package finki.ukim.mk.dbexamapi.domain.dtos.content;

import finki.ukim.mk.dbexamapi.domain.models.exams.Course;

public record FolderDto(
        Course course,
        String name
) {
}
