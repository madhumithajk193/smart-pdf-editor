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
public class PdfPermissionService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String protectWithPermissions(
            Long id,
            String ownerPassword,
            String userPassword,
            boolean allowPrinting,
            boolean allowCopying,
            boolean allowModification,
            String outputFileName) throws IOException {

        // 1. Find PDF
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "PDF not found with ID: " + id
                        )
                );

        // 2. Get original PDF
        Path inputPath =
                Paths.get(pdfFile.getFilePath());

        // 3. Check PDF exists
        if (!Files.exists(inputPath)) {
            throw new IOException(
                    "Source PDF file not found: "
                            + inputPath
            );
        }

        // 4. Validate passwords
        if (ownerPassword == null ||
                ownerPassword.isBlank()) {

            throw new IOException(
                    "Owner password cannot be empty"
            );
        }

        if (userPassword == null ||
                userPassword.isBlank()) {

            throw new IOException(
                    "User password cannot be empty"
            );
        }

        // 5. Create uploads folder
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path outputPath =
                uploadDir.resolve(outputFileName);

        // 6. Load PDF
        try (PDDocument document =
                     Loader.loadPDF(
                             inputPath.toFile()
                     )) {

            // 7. Create permissions
            AccessPermission permission =
                    new AccessPermission();

            // 8. Printing permission
            if (allowPrinting) {
                permission.setCanPrint(true);
            } else {
                permission.setCanPrint(false);
            }

            // 9. Copying permission
            if (allowCopying) {
                permission.setCanExtractContent(true);
            } else {
                permission.setCanExtractContent(false);
            }

            // 10. Modification permission
            if (allowModification) {
                permission.setCanModify(true);
            } else {
                permission.setCanModify(false);
            }

            // 11. Create protection policy
            StandardProtectionPolicy policy =
                    new StandardProtectionPolicy(
                            ownerPassword,
                            userPassword,
                            permission
                    );

            // 12. Use 256-bit encryption
            policy.setEncryptionKeyLength(256);

            // 13. Protect PDF
            document.protect(policy);

            // 14. Save protected PDF
            document.save(
                    outputPath.toFile()
            );
        }

        // 15. Return output path
        return outputPath
                .toAbsolutePath()
                .toString();
    }
}