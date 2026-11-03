package finki.ukim.mk.dbexamapi.web.v1.responses.content;

import org.springframework.core.io.Resource;

public record DocumentFileResponse(
        Resource file,
        String name,
        String mimeType
) {
}
