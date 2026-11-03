package finki.ukim.mk.dbexamapi.web.v1.extensions.content;

import finki.ukim.mk.dbexamapi.domain.dtos.content.FolderDto;
import finki.ukim.mk.dbexamapi.domain.models.content.Folder;
import finki.ukim.mk.dbexamapi.domain.models.exams.Course;
import finki.ukim.mk.dbexamapi.web.v1.requests.content.FolderRequest;
import finki.ukim.mk.dbexamapi.web.v1.responses.content.FolderResponse;

import java.util.List;

public final class FolderExtensions {

    private FolderExtensions() {
    }

    public static FolderDto toDto(FolderRequest request, Course course) {
        return new FolderDto(
                course,
                request.name()
        );
    }

    public static FolderResponse toResponse(Folder folder) {
        return new FolderResponse(
                folder.getId(),
                folder.getCourse().getId(),
                folder.getName()
        );
    }

    public static List<FolderResponse> toResponse(List<Folder> folders) {
        return folders.stream()
                .map(FolderExtensions::toResponse)
                .toList();
    }
}
