package finki.ukim.mk.dbexamapi.domain.dtos.content;

import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.domain.models.content.Folder;

public record ExamFolderDto(
        Exam exam,
        Folder folder,
        int sortOrder
) {
}
