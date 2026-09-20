package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfWatermarkService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String addWatermark(
            Long id,
            String watermarkText,
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

        // 4. Validate watermark text
        if (watermarkText == null ||
                watermarkText.isBlank()) {

            throw new IOException(
                    "Watermark text cannot be empty"
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

            // 8. Add watermark to every page
            for (PDPage page :
                    document.getPages()) {

                PDRectangle pageSize =
                        page.getMediaBox();

                float pageWidth =
                        pageSize.getWidth();

                float pageHeight =
                        pageSize.getHeight();

                // 9. Create content stream
                try (PDPageContentStream contentStream =
                             new PDPageContentStream(
                                     document,
                                     page,
                                     AppendMode.APPEND,
                                     true,
                                     true
                             )) {

                    // 10. Set font
                    PDType1Font font =
                            new PDType1Font(
                                    Standard14Fonts.FontName.HELVETICA_BOLD
                            );

                    contentStream.setFont(
                            font,
                            40
                    );

                    // 11. Set grey colour
                    contentStream.setNonStrokingColor(
                            Color.LIGHT_GRAY
                    );

                    // 12. Calculate text width
                    float textWidth =
                            font.getStringWidth(
                                    watermarkText
                            ) / 1000 * 40;

                    // 13. Calculate centre position
                    float x =
                            (pageWidth -
                                    textWidth) / 2;

                    float y =
                            pageHeight / 2;

                    // 14. Save graphics state
                    contentStream.saveGraphicsState();

                    // 15. Rotate watermark
                    contentStream.transform(
                            new org.apache.pdfbox.util.Matrix(
                                    (float) Math.cos(
                                            Math.toRadians(45)
                                    ),
                                    (float) Math.sin(
                                            Math.toRadians(45)
                                    ),
                                    (float) -Math.sin(
                                            Math.toRadians(45)
                                    ),
                                    (float) Math.cos(
                                            Math.toRadians(45)
                                    ),
                                    0,
                                    0
                            )
                    );

                    // 16. Write watermark
                    contentStream.beginText();

                    contentStream.newLineAtOffset(
                            x,
                            y
                    );

                    contentStream.showText(
                            watermarkText
                    );

                    contentStream.endText();

                    // 17. Restore graphics state
                    contentStream.restoreGraphicsState();
                }
            }

            // 18. Save watermarked PDF
            document.save(
                    outputPath.toFile()
            );
        }

        // 19. Return output path
        return outputPath
                .toAbsolutePath()
                .toString();
    }
}