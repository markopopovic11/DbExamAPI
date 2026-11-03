package finki.ukim.mk.dbexamapi.web.v1.requests.content;

import jakarta.validation.constraints.NotBlank;

public record FolderRequest(
        @NotBlank String courseId,
        @NotBlank String name
) {
}
