package com.empresa.onboarding.integration.storage;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.UUID;

@Component
public class MinioStorageService {
    private static final Logger log = LoggerFactory.getLogger(MinioStorageService.class);

    @Value("${onboarding.storage.minio.url:http://localhost:9000}")
    private String endpoint;
    @Value("${onboarding.storage.minio.access-key:minioadmin}")
    private String accessKey;
    @Value("${onboarding.storage.minio.secret-key:minioadmin}")
    private String secretKey;
    @Value("${onboarding.storage.minio.bucket-documentos:onboarding-documentos}")
    private String bucket;

    @PostConstruct
    public void init() {
        log.info("MinioStorageService configured for endpoint: {}", endpoint);
    }

    public String upload(String fileName, String mimeType, byte[] content) {
        String objectKey = UUID.randomUUID() + "/" + fileName;
        log.info("Simulando upload para MinIO: bucket={}, key={}, size={}b", bucket, objectKey, content.length);
        return objectKey;
    }

    public byte[] download(String objectKey) {
        log.info("Simulando download do MinIO: key={}", objectKey);
        return new byte[0];
    }

    public void delete(String objectKey) {
        log.info("Simulando delecao do MinIO: key={}", objectKey);
    }
}
