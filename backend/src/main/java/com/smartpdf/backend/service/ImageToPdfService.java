package com.smartpdf.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageToPdfService {

    private final Path uploadDir = Paths.get("uploads");

    public String convertImagesToPdf(
            MultipartFile[] images,
            String outputFileName) throws IOException {

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path outputPath = uploadDir.resolve(outputFileName);

        try (PDDocument document = new PDDocument()) {

            for (MultipartFile image : images) {

                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                PDImageXObject pdImage =
                        PDImageXObject.createFromByteArray(
                                document,
                                image.getBytes(),
                                image.getOriginalFilename()
                        );

                try (PDPageContentStream contentStream =
                             new PDPageContentStream(document, page)) {

                    contentStream.drawImage(
                            pdImage,
                            50,
                            100,
                            500,
                            600
                    );
                }
            }

            document.save(outputPath.toFile());
        }

        return outputPath.toAbsolutePath().toString();
    }
}