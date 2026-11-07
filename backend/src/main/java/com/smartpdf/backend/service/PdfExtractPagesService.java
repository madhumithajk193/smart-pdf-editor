
package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PdfExtractPagesService {

    private final PdfRepository pdfRepository;
    private final BackblazeStorageService storageService;

    private final Path uploadDir =
            Paths.get("uploads").toAbsolutePath().normalize();

    public PdfExtractPagesService(
            PdfRepository pdfRepository,
            BackblazeStorageService storageService) {

        this.pdfRepository = pdfRepository;
        this.storageService = storageService;
    }

    @Transactional
    public String extractPages(
            Long id,
            String pages,
            String outputFileName,
            String guestId) throws IOException {

        if (guestId == null || guestId.isBlank()) {
            throw new IOException("Guest identity is missing.");
        }

        // Find the original PDF belonging to this guest.
        PdfFile pdfFile = pdfRepository
                .findByIdAndGuestId(id, guestId)
                .orElseThrow(() ->
                        new IOException("PDF not found for this guest."));

        if (pages == null || pages.isBlank()) {
            throw new IOException("Pages cannot be empty.");
        }

        // Validate output filename.
        if (outputFileName == null || outputFileName.isBlank()) {
            outputFileName = "extracted.pdf";
        }

        outputFileName = Paths.get(outputFileName.trim())
                .getFileName()
                .toString();

        if (!outputFileName.toLowerCase().endsWith(".pdf")) {
            outputFileName += ".pdf";
        }

        if (!outputFileName.matches("[a-zA-Z0-9 _.-]+\\.pdf")
                || outputFileName.equals(".pdf")
                || outputFileName.equals("..pdf")) {
            throw new IOException("Invalid output PDF filename.");
        }

        Files.createDirectories(uploadDir);

        Path inputPath = null;
        boolean temporarySource = false;
        Path outputPath = null;

        try {

            String storedPath = pdfFile.getFilePath();

            if (storedPath == null || storedPath.isBlank()) {
                throw new IOException("Original PDF storage path is missing.");
            }

            // Support older PDFs that still exist locally.
            try {
                Path possibleLocalPath =
                        Paths.get(storedPath).toAbsolutePath().normalize();

                if (Files.isRegularFile(possibleLocalPath)
                        && Files.isReadable(possibleLocalPath)) {
                    inputPath = possibleLocalPath;
                }

            } catch (InvalidPathException ignored) {
                // Try Backblaze below.
            }

            // Download original PDF from Backblaze.
            if (inputPath == null) {

                inputPath = storageService.downloadFile(storedPath);

                if (inputPath == null
                        || !Files.isRegularFile(inputPath)
                        || !Files.isReadable(inputPath)) {

                    throw new IOException(
                            "Unable to download original PDF from Backblaze.");
                }

                temporarySource = true;
            }

            // Generate a unique filename to prevent overwriting.
            String uniqueName =
                    System.currentTimeMillis()
                    + "-"
                    + UUID.randomUUID().toString().substring(0, 8)
                    + "-"
                    + outputFileName;

            outputPath = uploadDir.resolve(uniqueName).normalize();

            if (!outputPath.getParent().equals(uploadDir)) {
                throw new IOException("Invalid output path.");
            }

            List<Integer> pageNumbers = parsePages(pages);

            // Extract requested pages.
            try (PDDocument sourceDocument =
                         Loader.loadPDF(inputPath.toFile());

                 PDDocument newDocument = new PDDocument()) {

                int totalPages = sourceDocument.getNumberOfPages();

                for (int pageNumber : pageNumbers) {

                    if (pageNumber < 1 || pageNumber > totalPages) {
                        throw new IOException(
                                "Invalid page number: " + pageNumber
                                + ". Total pages: " + totalPages);
                    }

                    newDocument.importPage(
                            sourceDocument.getPage(pageNumber - 1)
                    );
                }

                newDocument.save(outputPath.toFile());
            }

            // Upload extracted PDF to Backblaze.
            String objectKey = "extracted/" + uniqueName;

            storageService.uploadPath(outputPath, objectKey);

            // Save extracted PDF metadata and guest ownership.
            PdfFile extractedPdf = new PdfFile();

            extractedPdf.setFileName(uniqueName);
            extractedPdf.setFilePath(objectKey);
            extractedPdf.setFileSize(Files.size(outputPath));
            extractedPdf.setGuestId(guestId);

            pdfRepository.save(extractedPdf);

            // Return the actual stored filename.
            return uniqueName;

        } finally {

            // Remove temporary downloaded source.
            if (temporarySource && inputPath != null) {
                try {
                    Files.deleteIfExists(inputPath);
                } catch (IOException ignored) {
                    // Best-effort cleanup.
                }
            }

            // Remove local extracted copy after uploading.
            if (outputPath != null) {
                try {
                    Files.deleteIfExists(outputPath);
                } catch (IOException ignored) {
                    // Backblaze copy remains available.
                }
            }
        }
    }

    private List<Integer> parsePages(String pages)
            throws IOException {

        List<Integer> pageNumbers = new ArrayList<>();

        for (String part : pages.split(",")) {

            try {
                int page = Integer.parseInt(part.trim());

                if (page <= 0 || pageNumbers.contains(page)) {
                    throw new IOException(
                            "Page numbers must be positive and must not repeat.");
                }

                pageNumbers.add(page);

            } catch (NumberFormatException e) {
                throw new IOException("Invalid page number: " + part);
            }
        }

        return pageNumbers;
    }
}
