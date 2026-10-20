
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
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PdfMergeService {

    private final PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();

    public PdfMergeService(PdfRepository pdfRepository) {
        this.pdfRepository = pdfRepository;
    }

    @Transactional(readOnly = true)
    public String mergePdfs(String pdfIds, String outputFileName) throws IOException {

        // 1. Validate the input IDs.
        if (pdfIds == null || pdfIds.trim().isEmpty()) {
            throw new IOException("Please select at least two PDFs to merge.");
        }

        String[] idArray = pdfIds.split(",");
        List<Path> sourceFiles = new ArrayList<>();
        Set<Long> uniqueIds = new HashSet<>();

        for (String idStr : idArray) {
            if (idStr == null || idStr.trim().isEmpty()) {
                throw new IOException("Invalid PDF ID provided.");
            }

            final Long id;

            try {
                id = Long.parseLong(idStr.trim());
            } catch (NumberFormatException e) {
                throw new IOException("Invalid PDF ID: " + idStr, e);
            }

            if (id <= 0) {
                throw new IOException("PDF IDs must be positive numbers.");
            }

            if (!uniqueIds.add(id)) {
                throw new IOException("The same PDF cannot be selected more than once.");
            }

            // 2. Find each selected PDF in the database.
            PdfFile pdfFile = pdfRepository.findById(id)
                    .orElseThrow(() ->
                            new IOException("PDF not found with ID: " + id));

            String storedPath = pdfFile.getFilePath();

            if (storedPath == null || storedPath.trim().isEmpty()) {
                throw new IOException("No file path is stored for PDF ID: " + id);
            }

            Path inputPath;

            try {
                inputPath = Paths.get(storedPath).toAbsolutePath().normalize();
            } catch (InvalidPathException e) {
                throw new IOException("Invalid file path for PDF ID: " + id, e);
            }

            // Prevent an output filename from being used as a source accidentally.
            if (!Files.isRegularFile(inputPath) || !Files.isReadable(inputPath)) {
                throw new IOException("Source PDF is missing or unreadable: " + storedPath);
            }

            if (!storedPath.toLowerCase().endsWith(".pdf")) {
                throw new IOException("Selected file is not a PDF: " + storedPath);
            }

            // 3. Validate that each input is a readable PDF.
            try (PDDocument ignored = Loader.loadPDF(inputPath.toFile())) {
                // Successfully opened; close the document after validation.
            } catch (IOException e) {
                throw new IOException(
                        "Cannot read PDF with ID " + id +
                        ". The file may be damaged or password-protected.", e);
            }

            // Keep the order received from the frontend.
            sourceFiles.add(inputPath);
        }

        if (sourceFiles.size() < 2) {
            throw new IOException("Please select at least two PDFs to merge.");
        }

        // 4. Validate the output filename.
        if (outputFileName == null || outputFileName.trim().isEmpty()) {
            outputFileName = "merged.pdf";
        }

        String safeFileName = outputFileName.trim();

        if (!safeFileName.toLowerCase().endsWith(".pdf")) {
            safeFileName += ".pdf";
        }

        // Only allow a filename, not a directory or arbitrary path.
        if (!safeFileName.matches("[a-zA-Z0-9 _.-]+\\.pdf")) {
            throw new IOException(
                    "Invalid output filename. Use letters, numbers, spaces, "
                    + "underscores, hyphens, and periods.");
        }

        Files.createDirectories(uploadDir);

        Path outputPath = uploadDir.resolve(safeFileName).normalize();

        if (!outputPath.getParent().equals(uploadDir)) {
            throw new IOException("Invalid output file path.");
        }

        // Do not overwrite one of the source PDFs.
        for (Path source : sourceFiles) {
            if (source.equals(outputPath)) {
                throw new IOException(
                        "The output filename must be different from every source PDF.");
            }
        }

        // 5. Merge ALL selected PDFs, in the order selected.
        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(outputPath.toString());

        for (Path source : sourceFiles) {
            merger.addSource(source.toFile());
        }

        try {
            merger.mergeDocuments(null);
        } catch (IOException e) {
            // Remove a partial output if merging fails.
            Files.deleteIfExists(outputPath);
            throw new IOException("Failed to merge the selected PDFs: " + e.getMessage(), e);
        }

        // 6. Return the generated file path.
        return outputPath.toString();
    }
}
