
package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;
import org.apache.pdfbox.Loader;
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
public class PdfModifyService {

    private final PdfRepository pdfRepository;
    private final BackblazeStorageService storageService;

    private final Path uploadDir = Paths.get("uploads");

    public PdfModifyService(
            PdfRepository pdfRepository,
            BackblazeStorageService storageService) {
        this.pdfRepository = pdfRepository;
        this.storageService = storageService;
    }

    public String addTextToPdf(
            Long id,
            String guestId,
            String text,
            int pageNumber,
            float x,
            float y,
            float fontSize,
            String outputFileName) throws IOException {

        // Validate guest identity
        if (guestId == null || guestId.isBlank()) {
            throw new IOException("Guest identity is missing.");
        }

        // Only allow the guest to access their own PDF
        PdfFile pdfFile = pdfRepository
                .findByIdAndGuestId(id, guestId)
                .orElseThrow(() -> new IOException(
                        "PDF not found or you do not have permission to access it."
                ));

        if (text == null || text.isBlank()) {
            throw new IOException("Text cannot be empty.");
        }

        // Sanitize output filename
        if (outputFileName == null || outputFileName.isBlank()) {
            throw new IOException("Output filename is required.");
        }

        String safeFileName = Paths.get(outputFileName)
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9._-]", "_");

        if (!safeFileName.toLowerCase().endsWith(".pdf")) {
            safeFileName += ".pdf";
        }

        Files.createDirectories(uploadDir);

        Path outputPath = uploadDir.resolve(safeFileName).normalize();

        if (!outputPath.startsWith(uploadDir.toAbsolutePath().normalize())
                && !outputPath.startsWith(uploadDir.normalize())) {
            throw new IOException("Invalid output filename.");
        }

        // Download source PDF from B2, or use an existing local file
        Path inputPath;
        Path temporaryFile = null;

        try {
            Path possibleLocalPath = Paths.get(pdfFile.getFilePath());

            if (Files.isRegularFile(possibleLocalPath)) {
                inputPath = possibleLocalPath;
            } else {
                temporaryFile = storageService.downloadFile(
                        pdfFile.getFilePath()
                );
                inputPath = temporaryFile;
            }

            try (PDDocument document = Loader.loadPDF(inputPath.toFile())) {

                if (pageNumber < 1
                        || pageNumber > document.getNumberOfPages()) {
                    throw new IOException(
                            "Invalid page number. PDF has "
                                    + document.getNumberOfPages()
                                    + " pages."
                    );
                }

                // Keep font size within a reasonable range
                if (fontSize < 1) {
                    fontSize = 12;
                }

                if (fontSize > 100) {
                    fontSize = 100;
                }

                PDPage page = document.getPage(pageNumber - 1);

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

                    String[] lines = text.split("\\r?\\n");

                    for (int i = 0; i < lines.length; i++) {
                        contentStream.showText(lines[i]);

                        if (i < lines.length - 1) {
                            contentStream.newLineAtOffset(
                                    0,
                                    -fontSize * 1.2f
                            );
                        }
                    }

                    contentStream.endText();
                }

                document.save(outputPath.toFile());
            }

            return outputPath.toAbsolutePath().toString();

        } finally {
            // Remove the temporary B2 download
            if (temporaryFile != null) {
                Files.deleteIfExists(temporaryFile);
            }
        }
    }
}
