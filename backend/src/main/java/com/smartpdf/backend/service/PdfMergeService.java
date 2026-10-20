
package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PdfMergeService {

    private final PdfRepository pdfRepository;
    private final BackblazeStorageService storageService;

    private final Path uploadDir =
            Paths.get("uploads").toAbsolutePath().normalize();

    public PdfMergeService(
            PdfRepository pdfRepository,
            BackblazeStorageService storageService) {
        this.pdfRepository = pdfRepository;
        this.storageService = storageService;
    }

    @Transactional
    public String mergePdfs(String pdfIds, String outputFileName)
            throws IOException {

        if (pdfIds == null || pdfIds.isBlank()) {
            throw new IOException("Please select at least two PDFs.");
        }

        String[] ids = pdfIds.split(",");
        Set<Long> uniqueIds = new HashSet<>();
        List<Path> sourceFiles = new ArrayList<>();
        List<Path> temporaryFiles = new ArrayList<>();

        Path outputPath = null;

        try {
            // Download every selected PDF from local storage or Backblaze.
            for (String idText : ids) {
                final Long id;

                try {
                    id = Long.parseLong(idText.trim());
                } catch (NumberFormatException e) {
                    throw new IOException("Invalid PDF ID: " + idText, e);
                }

                if (id <= 0 || !uniqueIds.add(id)) {
                    throw new IOException(
                            "PDF IDs must be positive and must not repeat.");
                }

                PdfFile pdf = pdfRepository.findById(id)
                        .orElseThrow(() ->
                                new IOException("PDF not found: " + id));

                String storedPath = pdf.getFilePath();

                if (storedPath == null || storedPath.isBlank()) {
                    throw new IOException(
                            "Stored file reference is missing for PDF " + id);
                }

                Path source;

                // Support older records that point to an actual local file.
                Path possibleLocalPath = Paths.get(storedPath)
                        .toAbsolutePath().normalize();

                if (Files.isRegularFile(possibleLocalPath)
                        && Files.isReadable(possibleLocalPath)) {
                    source = possibleLocalPath;
                } else {
                    // New uploads store their Backblaze object key in filePath.
                    source = storageService.downloadFile(storedPath);
                    temporaryFiles.add(source);
                }

                if (!pdf.getFileName().toLowerCase().endsWith(".pdf")) {
                    throw new IOException(
                            "Selected file is not a PDF: " + pdf.getFileName());
                }

                try (PDDocument ignored = Loader.loadPDF(source.toFile())) {
                    // Validate that PDFBox can open the source file.
                } catch (IOException e) {
                    throw new IOException(
                            "Cannot read PDF ID " + id
                                    + ". It may be damaged or password-protected.",
                            e);
                }

                sourceFiles.add(source);
            }

            if (sourceFiles.size() < 2) {
                throw new IOException("Select at least two PDFs to merge.");
            }

            // Validate and normalize the output filename.
            if (outputFileName == null || outputFileName.isBlank()) {
                outputFileName = "merged.pdf";
            }

            String safeName = Paths.get(outputFileName.trim())
                    .getFileName().toString();

            if (!safeName.toLowerCase().endsWith(".pdf")) {
                safeName += ".pdf";
            }

            if (!safeName.matches("[a-zA-Z0-9 _.-]+\\.pdf")
                    || safeName.equals(".pdf")
                    || safeName.equals("..pdf")) {
                throw new IOException("Invalid output PDF filename.");
            }

            Files.createDirectories(uploadDir);

            // Avoid overwriting source PDFs or an earlier merged file.
            safeName = System.currentTimeMillis() + "-" + safeName;
            outputPath = uploadDir.resolve(safeName).normalize();

            if (!outputPath.getParent().equals(uploadDir)) {
                throw new IOException("Invalid output path.");
            }

            // Merge all PDFs in the order received.
            PDFMergerUtility merger = new PDFMergerUtility();
            merger.setDestinationFileName(outputPath.toString());

            for (Path source : sourceFiles) {
                merger.addSource(source.toFile());
            }

            merger.mergeDocuments(null);

            if (!Files.isRegularFile(outputPath)
                    || Files.size(outputPath) == 0) {
                throw new IOException("The merged PDF was not created.");
            }

            // Persist the merged PDF in Backblaze.
            String objectKey = "merged/" + safeName;
            storageService.uploadPath(outputPath, objectKey);

            // Save a database record so the merged PDF appears in the PDF list.
            PdfFile mergedPdf = new PdfFile();
            mergedPdf.setFileName(safeName);
            mergedPdf.setFilePath(objectKey);
            mergedPdf.setFileSize(Files.size(outputPath));
            pdfRepository.save(mergedPdf);

            // Preserve the existing controller's text response format.
            return outputPath.toString();

        } catch (Exception e) {
            if (outputPath != null) {
                try {
                    Files.deleteIfExists(outputPath);
                } catch (IOException cleanupError) {
                    e.addSuppressed(cleanupError);
                }
            }

            if (e instanceof IOException ioException) {
                throw ioException;
            }

            throw new IOException("PDF merge failed: " + e.getMessage(), e);

        } finally {
            // Temporary files downloaded from Backblaze are no longer needed.
            for (Path temp : temporaryFiles) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // Best-effort cleanup.
                }
            }
        }
    }
}
