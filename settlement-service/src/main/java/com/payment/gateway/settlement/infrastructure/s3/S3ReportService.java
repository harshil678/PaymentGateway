package com.payment.gateway.settlement.infrastructure.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ReportService {

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    private final S3Client s3Client;

    public String uploadReport(String key, String csvContent) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("text/csv")
                    .build();

            s3Client.putObject(request, RequestBody.fromString(csvContent));
            log.info("Report uploaded to S3: {}", key);
            return key;
        } catch (Exception e) {
            log.error("Failed to upload report to S3: {}", key, e);
            throw new RuntimeException("S3 upload failed for key: " + key, e);
        }
    }
}
