package finki.ukim.mk.dbexamapi.service.content;

import org.springframework.core.io.Resource;

import java.io.InputStream;

/**
 * Stores uploaded document files. The current implementation is Cloudflare R2; another store only
 * needs a new implementation. A file path is a key the store generates, never a name a client chose.
 */
public interface FileStorageService {

    /**
     * Returns the stored file's path, to save in documents.file_path.
     */
    String store(String originalFilename, String contentType, long sizeBytes, InputStream content);

    /**
     * Throws StoredFileDoesNotExistException when nothing is stored under the path.
     */
    Resource load(String filePath);

    void delete(String filePath);
}
