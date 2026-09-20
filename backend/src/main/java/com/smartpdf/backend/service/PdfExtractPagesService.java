package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfExtractPagesService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String extractPages(
            Long id,
            String pages,
            String outputFileName) throws IOException {

        // 1. Find PDF in database
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

        // 4. Check pages
        if (pages == null || pages.isBlank()) {
            throw new IOException(
                    "Pages cannot be empty"
            );
        }

        // 5. Create uploads folder
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 6. Create output path
        Path outputPath =
                uploadDir.resolve(outputFileName);

        // 7. Open original PDF
        try (PDDocument sourceDocument =
                     Loader.loadPDF(
                             inputPath.toFile()
                     );

             // Create new empty PDF
             PDDocument newDocument =
                     new PDDocument()) {

            // 8. Convert pages into numbers
            String[] pageArray =
                    pages.split(",");

            // 9. Copy selected pages
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

                // 10. Validate page number
                if (pageNumber < 1 ||
                        pageNumber >
                                sourceDocument
                                        .getNumberOfPages()) {

                    throw new IOException(
                            "Invalid page number: "
                                    + pageNumber
                    );
                }

                // 11. Get selected page
                newDocument.importPage(
                        sourceDocument.getPage(
                                pageNumber - 1
                        )
                );
            }
// 12. Save extracted PDF
            newDocument.save(
                    outputPath.toFile()
            );
        }

// 13. Save extracted PDF information to database
        PdfFile extractedPdf = new PdfFile();

        extractedPdf.setFileName(
                outputFileName
        );

        extractedPdf.setFilePath(
                outputPath.toAbsolutePath().toString()
        );

        extractedPdf.setFileSize(
                Files.size(outputPath)
        );

        pdfRepository.save(extractedPdf);

// 14. Return output path
        return outputPath
                .toAbsolutePath()
                .toString();

    }
}