package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfRotateService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String rotatePdf(
            Long id,
            String pages,
            int rotation,
            String outputFileName) throws IOException {

        // 1. Find PDF in database
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "PDF not found with ID: " + id
                        )
                );

        // 2. Get original PDF path
        Path inputPath = uploadDir
                .resolve(pdfFile.getFileName())
                .toAbsolutePath()
                .normalize();
        // 3. Check whether PDF exists
        System.out.println("========== ROTATE DEBUG ==========");
        System.out.println("Database path : " + pdfFile.getFilePath());
        System.out.println("Input path    : " + inputPath);
        System.out.println("Absolute path : " + inputPath.toAbsolutePath());
        System.out.println("File exists   : " + Files.exists(inputPath));
        System.out.println("Is file       : " + Files.isRegularFile(inputPath));
        System.out.println("Readable      : " + Files.isReadable(inputPath));
        System.out.println("==================================");

        if (!Files.exists(inputPath)) {
            throw new IOException(
                    "Source PDF file not found: "
                            + inputPath
            );
        }
        // 4. Validate rotation
        if (rotation != 90 &&
                rotation != 180 &&
                rotation != 270) {

            throw new IOException(
                    "Rotation must be 90, 180, or 270"
            );
        }

        // 5. Validate pages
        if (pages == null ||
                pages.isBlank()) {

            throw new IOException(
                    "Pages cannot be empty"
            );
        }

        // 6. Create uploads folder
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 7. Create output path
        Path outputPath =
                uploadDir.resolve(outputFileName);

        // 8. Load PDF
        try (PDDocument document =
                     Loader.loadPDF(
                             inputPath.toFile()
                     )) {

            // 9. Split page numbers
            String[] pageArray =
                    pages.split(",");

            // 10. Rotate selected pages
            for (String page : pageArray) {

                int pageNumber;

                try {

                    pageNumber =
                            Integer.parseInt(
                                    page.trim()
                            );

                } catch (NumberFormatException e) {

                    throw new IOException(
                            "Invalid page number: "
                                    + page
                    );
                }

                // Check page number
                if (pageNumber < 1 ||
                        pageNumber >
                                document
                                        .getNumberOfPages()) {

                    throw new IOException(
                            "Invalid page number: "
                                    + pageNumber
                    );
                }

                // Get page
                PDPage pdfPage =
                        document.getPage(
                                pageNumber - 1
                        );

                // Get current rotation
                int currentRotation =
                        pdfPage.getRotation();

                // Add new rotation
                int newRotation =
                        (currentRotation +
                                rotation) % 360;

                // Set rotation
                pdfPage.setRotation(
                        newRotation
                );
            }

            // 11. Save rotated PDF
            document.save(
                    outputPath.toFile()
            );
        }
        // Save rotated PDF information to database
        PdfFile rotatedPdf = new PdfFile();

        rotatedPdf.setFileName(outputFileName);

        rotatedPdf.setFilePath(
                outputPath.toAbsolutePath().toString()
        );

        rotatedPdf.setFileSize(
                Files.size(outputPath)
        );

        pdfRepository.save(rotatedPdf);

        // 12. Return output path
        return outputPath
                .toAbsolutePath()
                .toString();
    }
}