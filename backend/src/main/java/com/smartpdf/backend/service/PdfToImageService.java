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
public class PdfToImageService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String convertPdfToImages(
            Long id,
            String outputFolder) throws IOException {

        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException("PDF not found"));

        Path inputPath = Paths.get(pdfFile.getFilePath());

        if (!Files.exists(inputPath)) {
            throw new IOException("Source PDF file not found");
        }

        Path folder = uploadDir.resolve(outputFolder);

        if (!Files.exists(folder)) {
            Files.createDirectories(folder);
        }

        try (PDDocument document =
                     Loader.loadPDF(inputPath.toFile())) {

            PDFRenderer renderer =
                    new PDFRenderer(document);

            for (int i = 0; i < document.getNumberOfPages(); i++) {

                BufferedImage image =
                        renderer.renderImageWithDPI(
                                i,
                                300
                        );

                Path imagePath =
                        folder.resolve(
                                "page_" + (i + 1) + ".png"
                        );

                ImageIO.write(
                        image,
                        "PNG",
                        imagePath.toFile()
                );
            }
        }

        return folder.toAbsolutePath().toString();
    }
}