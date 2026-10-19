package com.smartpdf.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class BackblazeStorageService {

    private final S3Client s3Client;
    private final String bucketName;

    public BackblazeStorageService(
            @Value("${backblaze.endpoint}") String endpoint,
            @Value("${backblaze.region}") String region,
            @Value("${backblaze.bucket}") String bucketName,
            @Value("${backblaze.access-key}") String accessKey,
            @Value("${backblaze.secret-key}") String secretKey) {

        this.bucketName = bucketName;

        AwsBasicCredentials credentials =
                AwsBasicCredentials.create(
                        accessKey,
                        secretKey
                );

        this.s3Client = S3Client.builder()
                .endpointOverride(
                        URI.create(endpoint)
                )
                .region(
                        Region.of(region)
                )
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                credentials
                        )
                )
                .build();
    }

    // =========================================================
    // UPLOAD MULTIPART FILE
    // =========================================================

    public String uploadFile(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File is empty"
            );
        }

        String originalFilename =
                file.getOriginalFilename();

        if (originalFilename == null ||
                originalFilename.isBlank()) {

            throw new IllegalArgumentException(
                    "File name is missing"
            );
        }

        // Prevent directory/path problems
        String safeFilename =
                Path.of(originalFilename)
                        .getFileName()
                        .toString();

        // Unique Backblaze object key
        String objectKey =
                System.currentTimeMillis()
                        + "_"
                        + safeFilename;

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .contentType(
                                file.getContentType() != null
                                        ? file.getContentType()
                                        : "application/pdf"
                        )
                        .contentLength(file.getSize())
                        .build();

        s3Client.putObject(
                request,
                RequestBody.fromInputStream(
                        file.getInputStream(),
                        file.getSize()
                )
        );

        System.out.println(
                "Uploaded to Backblaze: "
                        + objectKey
        );

        // IMPORTANT:
        // Save THIS returned value into PdfFile.filePath
        return objectKey;
    }

    // =========================================================
    // DOWNLOAD FILE
    // =========================================================

    public Path downloadFile(String objectKey)
            throws IOException {

        if (objectKey == null ||
                objectKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Backblaze object key is empty"
            );
        }

        System.out.println(
                "Backblaze download:"
        );

        System.out.println(
                "Bucket = "
                        + bucketName
        );

        System.out.println(
                "Key = "
                        + objectKey
        );

        GetObjectRequest request =
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

        Path tempFile =
                Files.createTempFile(
                        "smartpdf-",
                        ".pdf"
                );

        try {

            try (InputStream inputStream =
                         s3Client.getObject(request)) {

                Files.copy(
                        inputStream,
                        tempFile,
                        StandardCopyOption
                                .REPLACE_EXISTING
                );
            }

            return tempFile;

        } catch (NoSuchKeyException e) {

            Files.deleteIfExists(tempFile);

            throw new IOException(
                    "Backblaze object not found. "
                            + "Bucket = "
                            + bucketName
                            + " Key = "
                            + objectKey,
                    e
            );

        } catch (S3Exception e) {

            Files.deleteIfExists(tempFile);

            if (e.statusCode() == 404) {

                throw new IOException(
                        "Backblaze object not found. "
                                + "Bucket = "
                                + bucketName
                                + " Key = "
                                + objectKey,
                        e
                );
            }

            throw new IOException(
                    "Backblaze download failed. "
                            + "Bucket = "
                            + bucketName
                            + " Key = "
                            + objectKey
                            + " Status = "
                            + e.statusCode(),
                    e
            );
        }
    }

    // =========================================================
    // UPLOAD PATH
    // Used for merged PDF
    // =========================================================

    public void uploadPath(
            Path file,
            String objectKey)
            throws IOException {

        if (file == null ||
                !Files.exists(file)) {

            throw new IOException(
                    "File does not exist: "
                            + file
            );
        }

        if (objectKey == null ||
                objectKey.isBlank()) {

            throw new IllegalArgumentException(
                    "Backblaze object key is empty"
            );
        }

        long fileSize =
                Files.size(file);

        PutObjectRequest request =
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .contentType(
                                "application/pdf"
                        )
                        .contentLength(fileSize)
                        .build();

        try (InputStream inputStream =
                     Files.newInputStream(file)) {

            s3Client.putObject(
                    request,
                    RequestBody.fromInputStream(
                            inputStream,
                            fileSize
                    )
            );
        }

        System.out.println(
                "Merged PDF uploaded: "
                        + objectKey
        );
    }

    // =========================================================
    // DELETE FILE
    // =========================================================

    public void deleteFile(String objectKey) {

        if (objectKey == null ||
                objectKey.isBlank()) {

            return;
        }

        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .build();

        s3Client.deleteObject(request);

        System.out.println(
                "Deleted from Backblaze: "
                        + objectKey
        );
    }
    public String getBucketName() {
        return bucketName;
    }
    }