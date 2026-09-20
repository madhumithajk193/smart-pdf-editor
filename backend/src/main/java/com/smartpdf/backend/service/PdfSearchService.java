package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfSearchService {

    private final PdfRepository pdfRepository;

    public PdfSearchService(PdfRepository pdfRepository) {
        this.pdfRepository = pdfRepository;
    }

    public List<Map<String, Object>> searchPdf(
            Long id,
            String keyword
    ) throws IOException {

        // =====================================================
        // 1. Validate keyword
        // =====================================================

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IOException("Search keyword cannot be empty");
        }

        keyword = keyword.trim();

        // =====================================================
        // 2. Find PDF from database
        // =====================================================

        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "PDF not found in database. ID: " + id
                        )
                );

        // =====================================================
        // 3. Get PDF path
        // =====================================================

        String storedPath = pdfFile.getFilePath();

        if (storedPath == null || storedPath.isBlank()) {
            throw new IOException(
                    "PDF file path is empty for ID: " + id
            );
        }

        Path pdfPath = Paths.get(storedPath);

        // =====================================================
        // 4. Check PDF exists
        // =====================================================

        if (!Files.exists(pdfPath)) {
            throw new IOException(
                    "PDF file does not exist: " + pdfPath
            );
        }

        if (!Files.isRegularFile(pdfPath)) {
            throw new IOException(
                    "PDF path is not a regular file: " + pdfPath
            );
        }

        // =====================================================
        // 5. Search PDF
        // =====================================================

        List<Map<String, Object>> results =
                new ArrayList<>();

        try (PDDocument document =
                     Loader.loadPDF(pdfPath.toFile())) {

            int totalPages = document.getNumberOfPages();

            for (int pageNumber = 1;
                 pageNumber <= totalPages;
                 pageNumber++) {

                PDFTextStripper stripper =
                        new PDFTextStripper();

                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);

                String pageText =
                        stripper.getText(document);

                if (pageText == null ||
                        pageText.isBlank()) {

                    continue;
                }

                String[] lines =
                        pageText.split("\\R");

                for (String line : lines) {

                    if (line == null ||
                            line.trim().isEmpty()) {

                        continue;
                    }

                    String trimmedLine =
                            line.trim();

                    /*
                     * Case-insensitive search.
                     */
                    if (trimmedLine
                            .toLowerCase()
                            .contains(
                                    keyword.toLowerCase()
                            )) {

                        Map<String, Object> result =
                                new LinkedHashMap<>();

                        result.put(
                                "page",
                                pageNumber
                        );

                        result.put(
                                "text",
                                trimmedLine
                        );

                        results.add(result);
                    }
                }
            }
        }

        // =====================================================
        // 6. Console information
        // =====================================================

        System.out.println(
                "===================================="
        );

        System.out.println(
                "PDF SEARCH SUCCESS"
        );

        System.out.println(
                "PDF ID      : " + id
        );

        System.out.println(
                "PDF Path    : " + pdfPath
        );

        System.out.println(
                "Keyword     : " + keyword
        );

        System.out.println(
                "Results     : " + results.size()
        );

        System.out.println(
                "===================================="
        );

        return results;
    }
}

