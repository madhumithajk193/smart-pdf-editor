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
public class PdfReorderService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String reorderPages(
            Long id,
            String pageOrder,
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

        // 4. Check page order
        if (pageOrder == null ||
                pageOrder.isBlank()) {

            throw new IOException(
                    "Page order cannot be empty"
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

             // Create new PDF
             PDDocument newDocument =
                     new PDDocument()) {

            // 8. Get total number of pages
            int totalPages =
                    sourceDocument.getNumberOfPages();

            // 9. Split page order
            String[] pageArray =
                    pageOrder.split(",");

            // 10. Add pages in requested order
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

                // 11. Validate page number
                if (pageNumber < 1 ||
                        pageNumber > totalPages) {

                    throw new IOException(
                            "Invalid page number: "
                                    + pageNumber
                    );
                }

                // 12. Import page
                newDocument.importPage(
                        sourceDocument.getPage(
                                pageNumber - 1
                        )
                );
            }

            // 13. Save reordered PDF
            newDocument.save(
                    outputPath.toFile()
            );
        }

        // 14. Return output file path
        return outputPath
                .toAbsolutePath()
                .toString();
    }
}