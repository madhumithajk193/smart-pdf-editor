package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfModifyService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String addTextToPdf(
            Long id,
            String text,
            int pageNumber,
            float x,
            float y,
            float fontSize,
            String outputFileName
    ) throws IOException {

        // Find PDF
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() -> new IOException("PDF not found"));

        // Source PDF
        Path inputPath = Paths.get(pdfFile.getFilePath());

        if (!Files.exists(inputPath)) {
            throw new IOException("Source PDF file not found: " + inputPath);
        }

        // Create uploads folder
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Output PDF
        Path outputPath = uploadDir.resolve(outputFileName);

        try (PDDocument document = Loader.loadPDF(inputPath.toFile())) {

            // Check page number
            if (pageNumber < 1 || pageNumber > document.getNumberOfPages()) {
                throw new IOException(
                        "Invalid page number. PDF has "
                                + document.getNumberOfPages()
                                + " pages."
                );
            }

            // PDFBox uses zero-based page index
            PDPage page = document.getPage(pageNumber - 1);

            // Prevent invalid font size
            if (fontSize < 1) {
                fontSize = 12;
            }

            if (fontSize > 100) {
                fontSize = 100;
            }

            try (PDPageContentStream contentStream =
                         new PDPageContentStream(
                                 document,
                                 page,
                                 PDPageContentStream.AppendMode.APPEND,
                                 true,
                                 true
                         )) {

                contentStream.beginText();

                contentStream.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA
                        ),
                        fontSize
                );

                contentStream.newLineAtOffset(x, y);

                // Handle multiple lines
                String[] lines = text.split("\\r?\\n");

                for (int i = 0; i < lines.length; i++) {

                    contentStream.showText(lines[i]);

                    if (i < lines.length - 1) {
                        contentStream.newLineAtOffset(0, -fontSize * 1.2f);
                    }
                }

                contentStream.endText();
            }

            document.save(outputPath.toFile());
        }

        return outputPath.toAbsolutePath().toString();
    }
}