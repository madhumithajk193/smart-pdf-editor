package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfHighlightService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String highlightArea(
            Long id,
            int pageNumber,
            float x,
            float y,
            float width,
            float height,
            String outputFileName
    ) throws IOException {

        // Find PDF from database
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() -> new IOException("PDF not found"));

        // Get original PDF path
        Path inputPath = Paths.get(pdfFile.getFilePath());

        // Check whether PDF exists
        if (!Files.exists(inputPath)) {
            throw new IOException("Source PDF file not found");
        }

        // Create uploads folder if it does not exist
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Create output path
        Path outputPath = uploadDir.resolve(outputFileName);

        // Load PDF
        try (PDDocument document = Loader.loadPDF(inputPath.toFile())) {

            // Check page number
            if (pageNumber < 0 ||
                    pageNumber >= document.getNumberOfPages()) {

                throw new IOException("Invalid page number");
            }

            // Get requested page
            PDPage page = document.getPage(pageNumber);

            // Add highlight rectangle
            try (PDPageContentStream contentStream =
                         new PDPageContentStream(
                                 document,
                                 page,
                                 PDPageContentStream.AppendMode.APPEND,
                                 true,
                                 true
                         )) {

                // Set highlight color
                contentStream.setNonStrokingColor(
                        new Color(255, 255, 0, 100)
                );

                // Draw rectangle
                contentStream.addRect(
                        x,
                        y,
                        width,
                        height
                );

                // Fill rectangle
                contentStream.fill();
            }

            // Save modified PDF
            document.save(outputPath.toFile());
        }

        // Return generated file path
        return outputPath.toAbsolutePath().toString();
    }
}