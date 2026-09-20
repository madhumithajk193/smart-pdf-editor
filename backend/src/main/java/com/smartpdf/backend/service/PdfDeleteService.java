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
public class PdfDeleteService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String deletePages(
            Long id,
            String pages,
            String outputFileName) throws IOException {

        // 1. Find PDF from database
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "PDF not found with ID: " + id));

        // 2. Get original PDF path
        Path inputPath =
                Paths.get(pdfFile.getFilePath());

        // 3. Check original PDF exists
        if (!Files.exists(inputPath)) {
            throw new IOException(
                    "Source PDF file not found: "
                            + inputPath);
        }

        // 4. Check pages input
        if (pages == null || pages.isBlank()) {
            throw new IOException(
                    "Pages cannot be empty");
        }

        // 5. Create uploads folder
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 6. Create output path
        Path outputPath =
                uploadDir.resolve(outputFileName);

        // 7. Open PDF
        try (PDDocument document =
                     Loader.loadPDF(inputPath.toFile())) {

            // 8. Convert pages into numbers
            String[] pageArray =
                    pages.split(",");

            // 9. Delete pages
            for (String page : pageArray) {

                int pageNumber;

                try {
                    pageNumber =
                            Integer.parseInt(
                                    page.trim());
                } catch (NumberFormatException e) {

                    throw new IOException(
                            "Invalid page number: "
                                    + page);
                }

                // PDF page number starts from 1
                int pageIndex =
                        pageNumber - 1;

                // Check valid page number
                if (pageNumber < 1 ||
                        pageNumber >
                                document.getNumberOfPages()) {

                    throw new IOException(
                            "Invalid page number: "
                                    + pageNumber);
                }

                // Delete page
                document.removePage(pageIndex);
            }

            // 10. Save new PDF
            document.save(
                    outputPath.toFile());
        }

        // 11. Return output file path
        return outputPath
                .toAbsolutePath()
                .toString();
    }
}