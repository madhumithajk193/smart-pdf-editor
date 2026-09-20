package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfMergeService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String mergePdfs(String pdfIds, String outputFileName) throws IOException {

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path outputPath = uploadDir.resolve(outputFileName);

        String[] idArray = pdfIds.split(",");
        List<Path> sourceFiles = new ArrayList<>();

        for (String idStr : idArray) {
            Long id = Long.parseLong(idStr.trim());

            PdfFile pdfFile = pdfRepository.findById(id)
                    .orElseThrow(() -> new IOException("PDF not found with ID: " + id));

            Path inputPath = Paths.get(pdfFile.getFilePath());

            if (!Files.exists(inputPath)) {
                throw new IOException("Source PDF file not found: " + inputPath);
            }

            sourceFiles.add(inputPath);
        }

        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(outputPath.toAbsolutePath().toString());

        for (Path file : sourceFiles) {
            merger.addSource(file.toFile());
        }

        merger.mergeDocuments(null);

        return outputPath.toAbsolutePath().toString();
    }
}