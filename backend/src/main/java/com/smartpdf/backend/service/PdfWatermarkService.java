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

        // 2. Download the original PDF from Backblaze
        Path inputPath =
                storageService.downloadFile(pdfFile.getFilePath());

        if (inputPath == null || !Files.exists(inputPath)) {
            throw new IOException("Could not download source PDF from storage.");
        }

        // 3. Create a temporary output directory
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

        // Use a unique filename to avoid overwriting another PDF
        String uniqueFileName =
                UUID.randomUUID() + "_" + safeName;

        Path outputPath = tempDir.resolve(uniqueFileName);

        try {
            // 4. Load the source PDF
            try (PDDocument document =
                         Loader.loadPDF(inputPath.toFile())) {

                PDType1Font font = new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA_BOLD
                );

                // 5. Add watermark to every page
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

                        contentStream.setFont(font, 40);

                        contentStream.setNonStrokingColor(
                                Color.LIGHT_GRAY
                        );

                        float textWidth =
                                font.getStringWidth(watermarkText)
                                        / 1000 * 40;

                        float x = (pageWidth - textWidth) / 2;
                        float y = pageHeight / 2;

                        contentStream.saveGraphicsState();

                        contentStream.transform(
                                new org.apache.pdfbox.util.Matrix(
                                        (float) Math.cos(Math.toRadians(45)),
                                        (float) Math.sin(Math.toRadians(45)),
                                        (float) -Math.sin(Math.toRadians(45)),
                                        (float) Math.cos(Math.toRadians(45)),
                                        0,
                                        0
                                )
                        );

                        contentStream.beginText();
                        contentStream.newLineAtOffset(x, y);
                        contentStream.showText(watermarkText);
                        contentStream.endText();

                        contentStream.restoreGraphicsState();
                    }
                }

                // 6. Save the watermarked PDF temporarily
                document.save(outputPath.toFile());
            }

            // 7. Upload the generated PDF to Backblaze
            String objectKey = "watermarked/" + uniqueFileName;

            storageService.uploadPath(outputPath, objectKey);

            // 8. Save the new PDF record in the database
            PdfFile watermarkedPdf = new PdfFile();

            watermarkedPdf.setFileName(uniqueFileName);
            watermarkedPdf.setFilePath(objectKey);
            watermarkedPdf.setFileSize(Files.size(outputPath));
            watermarkedPdf.setGuestId(guestId);

            pdfRepository.save(watermarkedPdf);

            // 9. Return the actual generated filename
            return uniqueFileName;

        } finally {
            // Remove only our temporary output file and directory
            Files.deleteIfExists(outputPath);
            Files.deleteIfExists(tempDir);
        }
    }
}
