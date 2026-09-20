package com.smartpdf.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfEditService {

    // Folder where edited PDFs will be saved
    private final Path uploadDir = Paths.get("uploads");

    // Create a new PDF using edited text
    public String createEditedPdf(String text, String fileName) throws IOException {

        // Create uploads folder if it does not exist
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Create the output file path
        Path filePath = uploadDir.resolve(fileName);

        // Create a new PDF document
        try (PDDocument document = new PDDocument()) {

            // Add a new page
            PDPage page = new PDPage();
            document.addPage(page);

            // Add text to the page
            try (PDPageContentStream contentStream =
                         new PDPageContentStream(document, page)) {

                contentStream.beginText();

                // Set font
                contentStream.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        12
                );

                // Set starting position
                contentStream.newLineAtOffset(50, 750);

                // Write text
                contentStream.showText(text);

                contentStream.endText();
            }

            // Save the PDF
            document.save(filePath.toFile());
        }

        // Return the saved file path
        return filePath.toAbsolutePath().toString();
    }
}