package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfSignatureService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir = Paths.get("uploads");

    public String addSignature(
            Long id,
            MultipartFile signature,
            int pageNumber,
            float x,
            float y,
            float width,
            float height,
            String outputFileName) throws IOException {

        // 1. Find PDF
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "PDF not found with ID: " + id
                        )
                );

        // 2. Get PDF path
        Path inputPath =
                Paths.get(pdfFile.getFilePath());

        if (!Files.exists(inputPath)) {
            throw new IOException(
                    "Source PDF file not found: "
                            + inputPath
            );
        }

        // 3. Validate signature
        if (signature == null ||
                signature.isEmpty()) {

            throw new IOException(
                    "Signature image cannot be empty"
            );
        }

        // 4. Create uploads folder
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path outputPath =
                uploadDir.resolve(outputFileName);

        // 5. Load PDF
        try (PDDocument document =
                     Loader.loadPDF(inputPath.toFile())) {

            int totalPages =
                    document.getNumberOfPages();

            // 6. Validate page number
            if (pageNumber < 1 ||
                    pageNumber > totalPages) {

                throw new IOException(
                        "Invalid page number: "
                                + pageNumber
                );
            }

            // 7. Get selected page
            PDPage page =
                    document.getPage(pageNumber - 1);

            // 8. Convert signature image
            PDImageXObject signatureImage =
                    PDImageXObject.createFromByteArray(
                            document,
                            signature.getBytes(),
                            signature.getOriginalFilename()
                    );

            // 9. Add signature to page
            try (PDPageContentStream contentStream =
                         new PDPageContentStream(
                                 document,
                                 page,
                                 PDPageContentStream.AppendMode.APPEND,
                                 true,
                                 true
                         )) {

                contentStream.drawImage(
                        signatureImage,
                        x,
                        y,
                        width,
                        height
                );
            }

            // 10. Save signed PDF
            document.save(
                    outputPath.toFile()
            );
        }

        return outputPath
                .toAbsolutePath()
                .toString();
    }
}