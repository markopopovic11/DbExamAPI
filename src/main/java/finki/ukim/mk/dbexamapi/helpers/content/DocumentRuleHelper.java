package finki.ukim.mk.dbexamapi.helpers.content;

import finki.ukim.mk.dbexamapi.domain.dtos.content.DocumentDto;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.DocumentFileEmptyException;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.DocumentSourceInvalidException;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * Rules for what a document holds.
 */
@Component
public class DocumentRuleHelper {

    // A document is markdown written in the app, an uploaded file or a link: exactly one of them.
    public void requireExactlyOneSource(DocumentDto documentDto) {
        long sources = Stream.of(documentDto.contentMd(), documentDto.filePath(), documentDto.url())
                .filter(Objects::nonNull)
                .count();
        if (sources != 1) {
            throw new DocumentSourceInvalidException();
        }
    }

    public void requireFileNotEmpty(DocumentDto documentDto) {
        if (documentDto.sizeBytes() == null || documentDto.sizeBytes() == 0) {
            throw new DocumentFileEmptyException();
        }
    }
}
