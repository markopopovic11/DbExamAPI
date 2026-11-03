package finki.ukim.mk.dbexamapi.helpers.content;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Names for stored files. A stored name is generated, never the name a client sent.
 */
@Component
public class FileNameHelper {

    private static final String SAFE_EXTENSION = "[A-Za-z0-9]{1,10}";

    // A new random name that keeps a short alphanumeric extension like .sql or .pdf,
    // so the stored file stays recognisable.
    public String newStoredName(String originalFilename) {
        return NanoIdUtils.randomNanoId() + extensionOf(originalFilename);
    }

    private String extensionOf(String originalFilename) {
        return Optional.ofNullable(originalFilename)
                .filter(name -> name.lastIndexOf('.') >= 0)
                .map(name -> name.substring(name.lastIndexOf('.') + 1))
                .filter(extension -> extension.matches(SAFE_EXTENSION))
                .map(extension -> "." + extension.toLowerCase())
                .orElse("");
    }
}
