package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfThumbnailService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String generateThumbnails(
            Long id,
            String outputFolder) throws IOException {

        // 1. Find PDF from database
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "PDF not found with ID: " + id
                        )
                );

        // 2. Get original PDF path
        Path inputPath =
                Paths.get(pdfFile.getFilePath());

        // 3. Check source PDF
        if (!Files.exists(inputPath)) {
            throw new IOException(
                    "Source PDF file not found: "
                            + inputPath
            );
        }

        // 4. Create uploads folder
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 5. Create thumbnail folder
        Path thumbnailFolder =
                uploadDir.resolve(outputFolder);

        if (!Files.exists(thumbnailFolder)) {
            Files.createDirectories(thumbnailFolder);
        }

        // 6. Load PDF
        try (PDDocument document =
                     Loader.loadPDF(inputPath.toFile())) {

            PDFRenderer renderer =
                    new PDFRenderer(document);

            // 7. Create thumbnail for every page
            for (int i = 0;
                 i < document.getNumberOfPages();
                 i++) {

                BufferedImage image =
                        renderer.renderImageWithDPI(
                                i,
                                80
                        );

                Path thumbnailPath =
                        thumbnailFolder.resolve(
                                "page_" + (i + 1) + ".png"
                        );

                ImageIO.write(
                        image,
                        "PNG",
                        thumbnailPath.toFile()
                );
            }
        }

        // 8. Return thumbnail folder path
        return thumbnailFolder
                .toAbsolutePath()
                .toString();
    }
}