package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfCompressionService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String compressPdf(
            Long id,
            String outputFileName) throws IOException {

        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() -> new IOException("PDF not found"));

        Path inputPath = Paths.get(pdfFile.getFilePath());

        if (!Files.exists(inputPath)) {
            throw new IOException("Source PDF file not found");
        }

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path outputPath = uploadDir.resolve(outputFileName);

        try (PDDocument document = Loader.loadPDF(inputPath.toFile())) {

            // Save using PDFBox optimization
            document.save(outputPath.toFile());
        }

        return outputPath.toAbsolutePath().toString();
    }
}