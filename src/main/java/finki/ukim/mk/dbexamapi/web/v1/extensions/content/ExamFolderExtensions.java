package finki.ukim.mk.dbexamapi.web.v1.extensions.content;

import finki.ukim.mk.dbexamapi.domain.dtos.content.ExamFolderDto;
import finki.ukim.mk.dbexamapi.domain.models.content.ExamFolder;
import finki.ukim.mk.dbexamapi.domain.models.content.Folder;
import finki.ukim.mk.dbexamapi.domain.models.exams.Exam;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.ExamFolderRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.ExamFolderResponse;

import java.util.List;

public final class ExamFolderExtensions {

    private ExamFolderExtensions() {
    }

    public static ExamFolderDto toDto(ExamFolderRequest request, Exam exam, Folder folder) {
        return new ExamFolderDto(
                exam,
                folder,
                request.sortOrder()
        );
    }

    public static ExamFolderResponse toResponse(ExamFolder examFolder) {
        return new ExamFolderResponse(
                examFolder.getId(),
                examFolder.getExam().getId(),
                examFolder.getFolder().getId(),
                examFolder.getSortOrder()
        );
    }

    public static List<ExamFolderResponse> toResponse(List<ExamFolder> examFolders) {
        return examFolders.stream()
                .map(ExamFolderExtensions::toResponse)
                .toList();
    }
}
