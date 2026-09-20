package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfPasswordService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String protectPdf(
            Long id,
            String password,
            String outputFileName) throws IOException {

        // 1. Find PDF from database
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "PDF not found with ID: " + id
                        )
                );

        // 2. Get original PDF path
        Path inputPath =
                Paths.get(pdfFile.getFilePath());

        // 3. Check source PDF exists
        if (!Files.exists(inputPath)) {
            throw new IOException(
                    "Source PDF file not found: "
                            + inputPath
            );
        }

        // 4. Check password
        if (password == null ||
                password.isBlank()) {

            throw new IOException(
                    "Password cannot be empty"
            );
        }

        // 5. Create uploads folder
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 6. Create output path
        Path outputPath =
                uploadDir.resolve(outputFileName);

        // 7. Load original PDF
        try (PDDocument document =
                     Loader.loadPDF(
                             inputPath.toFile()
                     )) {

            // 8. Create access permissions
            AccessPermission accessPermission =
                    new AccessPermission();

            // 9. Create protection policy
            StandardProtectionPolicy policy =
                    new StandardProtectionPolicy(
                            password,
                            password,
                            accessPermission
                    );

            // 10. Set encryption key length
            policy.setEncryptionKeyLength(256);

            // 11. Protect PDF
            document.protect(policy);

            // 12. Save protected PDF
            document.save(
                    outputPath.toFile()
            );
        }

        // 13. Return output path
        return outputPath
                .toAbsolutePath()
                .toString();
    }
}