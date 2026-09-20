package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfPageNumberService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String addPageNumbers(
            Long id,
            int startingPageNumber,
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

        // 4. Validate starting page number
        if (startingPageNumber < 1) {
            throw new IOException(
                    "Starting page number must be at least 1"
            );
        }

        // 5. Create uploads folder
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 6. Create output path
        Path outputPath =
                uploadDir.resolve(outputFileName);

        // 7. Load PDF
        try (PDDocument document =
                     Loader.loadPDF(
                             inputPath.toFile()
                     )) {

            int pageNumber =
                    startingPageNumber;

            // 8. Loop through every page
            for (PDPage page :
                    document.getPages()) {

                PDRectangle pageSize =
                        page.getMediaBox();

                float pageWidth =
                        pageSize.getWidth();

                // 9. Create content stream
                try (PDPageContentStream contentStream =
                             new PDPageContentStream(
                                     document,
                                     page,
                                     PDPageContentStream.AppendMode.APPEND,
                                     true,
                                     true
                             )) {

                    // 10. Set font
                    contentStream.setFont(
                            new PDType1Font(
                                    Standard14Fonts.FontName.HELVETICA
                            ),
                            12
                    );

                    // 11. Set text colour
                    contentStream.setNonStrokingColor(
                            0,
                            0,
                            0
                    );

                    // 12. Page number text
                    String text =
                            String.valueOf(
                                    pageNumber
                            );

                    // 13. Calculate text position
                    float textWidth =
                            new PDType1Font(
                                    Standard14Fonts.FontName.HELVETICA
                            ).getStringWidth(text)
                                    / 1000 * 12;

                    float x =
                            (pageWidth -
                                    textWidth) / 2;

                    float y =
                            20;

                    // 14. Write page number
                    contentStream.beginText();

                    contentStream.newLineAtOffset(
                            x,
                            y
                    );

                    contentStream.showText(
                            text
                    );

                    contentStream.endText();
                }

                // 15. Move to next page number
                pageNumber++;
            }

            // 16. Save numbered PDF
            document.save(
                    outputPath.toFile()
            );
        }

        // 17. Return output path
        return outputPath
                .toAbsolutePath()
                .toString();
    }
}