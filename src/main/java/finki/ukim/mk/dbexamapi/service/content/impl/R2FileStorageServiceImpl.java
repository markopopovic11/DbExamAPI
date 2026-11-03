package finki.ukim.mk.dbexamapi.service.content.impl;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import finki.ukim.mk.dbexamapi.config.R2Properties;
import finki.ukim.mk.dbexamapi.domain.exceptions.content.StoredFileDoesNotExistException;
import finki.ukim.mk.dbexamapi.service.content.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;

@Slf4j
@Service
public class R2FileStorageServiceImpl implements FileStorageService {

    private static final String KEY_PREFIX = "documents/";

    private final S3Client r2Client;
    private final String bucket;

    public R2FileStorageServiceImpl(S3Client r2Client, R2Properties r2Properties) {
        this.r2Client = r2Client;
        this.bucket = r2Properties.bucket();
    }

    @Override
    public String store(String originalFilename, String contentType, long sizeBytes, InputStream content) {
        String filePath = KEY_PREFIX + NanoIdUtils.randomNanoId() + extensionOf(originalFilename);
        r2Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(filePath)
                        .contentType(contentType)
                        .contentLength(sizeBytes)
                        .build(),
                RequestBody.fromInputStream(content, sizeBytes));
        log.info("Stored file in R2: {}", filePath);
        return filePath;
    }

    @Override
    public Resource load(String filePath) {
        try {
            return new InputStreamResource(r2Client.getObject(
                    GetObjectRequest.builder().bucket(bucket).key(filePath).build()));
        } catch (NoSuchKeyException exception) {
            throw new StoredFileDoesNotExistException(filePath);
        }
    }

    @Override
    public void delete(String filePath) {
        r2Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(filePath).build());
        log.info("Deleted file from R2: {}", filePath);
    }

    /**
     * Keeps a short alphanumeric extension like .sql or .pdf, so the stored file stays recognisable.
     */
    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        String extension = originalFilename.substring(dot + 1);
        return extension.matches("[A-Za-z0-9]{1,10}") ? "." + extension.toLowerCase() : "";
    }
}
