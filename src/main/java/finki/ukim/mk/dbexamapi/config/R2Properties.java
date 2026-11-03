package finki.ukim.mk.dbexamapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.storage.r2")
public record R2Properties(
        String accountId,
        String bucket,
        String accessKeyId,
        String secretAccessKey
) {

    public String endpoint() {
        return String.format("https://%s.r2.cloudflarestorage.com", accountId);
    }
}
