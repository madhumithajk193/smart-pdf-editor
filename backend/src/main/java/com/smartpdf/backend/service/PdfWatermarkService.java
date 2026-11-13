
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

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class PdfWatermarkService {

    private final PdfRepository pdfRepository;
    private final BackblazeStorageService storageService;

    public PdfWatermarkService(
            PdfRepository pdfRepository,
            BackblazeStorageService storageService) {

        this.pdfRepository = pdfRepository;
        this.storageService = storageService;
    }

    public String addWatermark(
            Long id,
            String watermarkText,
            String outputFileName,
            String guestId) throws IOException {

        if (guestId == null || guestId.isBlank()) {
            throw new IOException("Guest identity is missing.");
        }

        if (watermarkText == null || watermarkText.isBlank()) {
            throw new IOException("Watermark text cannot be empty.");
        }

        // 1. Find the source PDF belonging to this guest
        PdfFile pdfFile = pdfRepository
                .findByIdAndGuestId(id, guestId)
                .orElseThrow(() ->
                        new IOException("PDF not found for this guest.")
                );

        if (pdfFile.getFilePath() == null ||
                pdfFile.getFilePath().isBlank()) {
            throw new IOException("PDF storage path is missing.");
        }

        // 2. Download original PDF from Backblaze
        Path inputPath =
                storageService.downloadFile(pdfFile.getFilePath());

        if (inputPath == null || !Files.exists(inputPath)) {
            throw new IOException(
                    "Could not download source PDF from storage."
            );
        }

        // 3. Create temporary output directory
        Path tempDir = Files.createTempDirectory("watermark-");

        String safeName = outputFileName == null
                ? "watermarked.pdf"
                : outputFileName.trim()
                        .replaceAll("[^a-zA-Z0-9._-]", "_");

        if (safeName.isBlank()) {
            safeName = "watermarked.pdf";
        }

        if (!safeName.toLowerCase().endsWith(".pdf")) {
            safeName += ".pdf";
        }

        // 4. Generate unique filename
        String uniqueFileName =
                UUID.randomUUID() + "_" + safeName;

        Path outputPath = tempDir.resolve(uniqueFileName);

        try {

            // 5. Load original PDF
            try (PDDocument document =
                         Loader.loadPDF(inputPath.toFile())) {

                PDType1Font font = new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                );

                // 6. Add watermark to every page
                for (PDPage page : document.getPages()) {

                    PDRectangle pageSize = page.getMediaBox();

                    float pageWidth = pageSize.getWidth();
                    float pageHeight = pageSize.getHeight();

                    try (PDPageContentStream contentStream =
                                 new PDPageContentStream(
                                         document,
                                         page,
                                         AppendMode.APPEND,
                                         true,
                                         true
                                 )) {

                        // Set watermark font
                        contentStream.setFont(font, 40);

                        // Set watermark colour
                        contentStream.setNonStrokingColor(
                                Color.LIGHT_GRAY
                        );

                        // Calculate watermark text width
                        float textWidth =
                                font.getStringWidth(watermarkText)
                                        / 1000 * 40;

                        // 7. Calculate page centre
                        float centerX = pageWidth / 2;
                        float centerY = pageHeight / 2;

                        // 8. Save graphics state
                        contentStream.saveGraphicsState();

                        // 9. Rotate around the page centre
                        contentStream.transform(
                                org.apache.pdfbox.util.Matrix
                                        .getRotateInstance(
                                                Math.toRadians(45),
                                                centerX,
                                                centerY
                                        )
                        );

                        // 10. Write watermark at the centre
                        contentStream.beginText();

                        contentStream.newLineAtOffset(
                                -textWidth / 2,
                                -20
                        );

                        contentStream.showText(watermarkText);

                        contentStream.endText();

                        // 11. Restore graphics state
                        contentStream.restoreGraphicsState();
                    }
                }

                // 12. Save watermarked PDF temporarily
                document.save(outputPath.toFile());
            }

            // 13. Upload generated PDF to Backblaze
            String objectKey = "watermarked/" + uniqueFileName;

            storageService.uploadPath(outputPath, objectKey);

            // 14. Save PDF record in database
            PdfFile watermarkedPdf = new PdfFile();

            watermarkedPdf.setFileName(uniqueFileName);
            watermarkedPdf.setFilePath(objectKey);
            watermarkedPdf.setFileSize(Files.size(outputPath));
            watermarkedPdf.setGuestId(guestId);

            pdfRepository.save(watermarkedPdf);

            // 15. Return actual generated filename
            return uniqueFileName;

        } finally {

            // Clean up temporary output
            Files.deleteIfExists(outputPath);
            Files.deleteIfExists(tempDir);
        }
    }
}
