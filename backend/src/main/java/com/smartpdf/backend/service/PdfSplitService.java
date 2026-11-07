
package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

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
public class PdfSplitService {

    private final PdfRepository pdfRepository;
    private final BackblazeStorageService storageService;

    private final Path uploadDir =
            Paths.get("uploads").toAbsolutePath().normalize();

    public PdfSplitService(
            PdfRepository pdfRepository,
            BackblazeStorageService storageService) {

        this.pdfRepository = pdfRepository;
        this.storageService = storageService;
    }

    @Transactional
    public List<String> splitPdf(
            Long id,
            String pages,
            String outputPrefix,
            String guestId) throws IOException {

        if (guestId == null || guestId.isBlank()) {
            throw new IOException("Guest identity is missing.");
        }

        // Find the PDF only if it belongs to this guest.
        PdfFile pdfFile = pdfRepository
                .findByIdAndGuestId(id, guestId)
                .orElseThrow(() ->
                        new IOException("PDF not found for this guest."));

        Files.createDirectories(uploadDir);

        Path inputPath = null;
        boolean temporarySource = false;

        List<Path> generatedFiles = new ArrayList<>();
        List<String> savedFiles = new ArrayList<>();

        try {

            String storedPath = pdfFile.getFilePath();

            if (storedPath == null || storedPath.isBlank()) {
                throw new IOException("PDF storage path is missing.");
            }

            // Support older files that may still exist locally.
            try {
                Path possibleLocalPath =
                        Paths.get(storedPath).toAbsolutePath().normalize();

                if (Files.isRegularFile(possibleLocalPath)
                        && Files.isReadable(possibleLocalPath)) {
                    inputPath = possibleLocalPath;
                }
            } catch (InvalidPathException ignored) {
                // Try Backblaze storage below.
            }

            // Download from Backblaze when no local source exists.
            if (inputPath == null) {

                inputPath = storageService.downloadFile(storedPath);

                if (inputPath == null
                        || !Files.isRegularFile(inputPath)
                        || !Files.isReadable(inputPath)) {

                    throw new IOException(
                            "Unable to download original PDF from storage.");
                }

                temporarySource = true;
            }

            if (outputPrefix == null || outputPrefix.isBlank()) {
                outputPrefix = "split";
            }

            outputPrefix = Paths.get(outputPrefix.trim())
                    .getFileName()
                    .toString();

            if (!outputPrefix.matches("[a-zA-Z0-9_-]+")) {
                throw new IOException("Invalid output prefix.");
            }

            List<Integer> pageNumbers = parsePages(pages);

            try (PDDocument sourceDoc =
                         Loader.loadPDF(inputPath.toFile())) {

                int totalPages = sourceDoc.getNumberOfPages();

                for (int pageNumber : pageNumbers) {

                    if (pageNumber < 1 || pageNumber > totalPages) {
                        throw new IOException(
                                "Invalid page number: " + pageNumber
                                + ". Total pages: " + totalPages);
                    }

                    String uniqueName =
                            System.currentTimeMillis() + "-"
                            + UUID.randomUUID().toString().substring(0, 8)
                            + "-" + outputPrefix
                            + "_page_" + pageNumber + ".pdf";

                    Path outputPath =
                            uploadDir.resolve(uniqueName).normalize();

                    if (!outputPath.getParent().equals(uploadDir)) {
                        throw new IOException("Invalid output path.");
                    }

                    try (PDDocument newDoc = new PDDocument()) {

                        PDPage page =
                                sourceDoc.getPage(pageNumber - 1);

                        newDoc.importPage(page);
                        newDoc.save(outputPath.toFile());
                    }

                    generatedFiles.add(outputPath);

                    // Upload generated PDF to Backblaze.
                    String objectKey = "split/" + uniqueName;

                    storageService.uploadPath(outputPath, objectKey);

                    // Save metadata and guest ownership.
                    PdfFile splitPdf = new PdfFile();

                    splitPdf.setFileName(uniqueName);
                    splitPdf.setFilePath(objectKey);
                    splitPdf.setFileSize(Files.size(outputPath));
                    splitPdf.setGuestId(guestId);

                    pdfRepository.save(splitPdf);

                    // Return the actual database filename.
                    savedFiles.add(uniqueName);
                }
            }

            return savedFiles;

        } finally {

            // Delete downloaded temporary source.
            if (temporarySource && inputPath != null) {
                try {
                    Files.deleteIfExists(inputPath);
                } catch (IOException ignored) {
                    // Best-effort cleanup.
                }
            }

            // Delete local generated copies after uploading.
            for (Path generatedFile : generatedFiles) {
                try {
                    Files.deleteIfExists(generatedFile);
                } catch (IOException ignored) {
                    // Backblaze copy remains available.
                }
            }
        }
    }

    private List<Integer> parsePages(String pages)
            throws IOException {

        if (pages == null || pages.isBlank()) {
            throw new IOException("Pages cannot be empty.");
        }

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
