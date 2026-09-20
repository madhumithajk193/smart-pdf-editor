package com.smartpdf.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfTextService {

    public String extractText(String filePath) throws IOException {

        // Convert file path to Path
        Path path = Paths.get(filePath);

        // Load PDF
        try (PDDocument document = Loader.loadPDF(path.toFile())) {

            // Create text stripper
            PDFTextStripper pdfTextStripper =
                    new PDFTextStripper();

            // Extract text
            return pdfTextStripper.getText(document);
        }
    }
}