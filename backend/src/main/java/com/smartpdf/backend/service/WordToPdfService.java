package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class WordToPdfService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("uploads")
                    .toAbsolutePath()
                    .normalize();

    public String convertWordToPdf(
            Long id,
            String outputFileName
    ) throws IOException {

        // ==========================================
        // 1. FIND DATABASE RECORD
        // ==========================================

        PdfFile file = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "File not found in database. ID: " + id
                        )
                );

        System.out.println("=================================");
        System.out.println("WORD TO PDF");
        System.out.println("ID            : " + id);
        System.out.println("Database name : " + file.getFileName());
        System.out.println("Database path : " + file.getFilePath());
        System.out.println("Upload folder : " + uploadDir);
        System.out.println("=================================");


        // ==========================================
        // 2. FIND WORD FILE
        // ==========================================

        Path inputPath = null;

        // Try database path first
        if (file.getFilePath() != null &&
                !file.getFilePath().isBlank()) {

            Path databasePath =
                    Paths.get(file.getFilePath())
                            .toAbsolutePath()
                            .normalize();

            if (Files.exists(databasePath)) {
                inputPath = databasePath;
            }
        }

        // Fallback to uploads folder
        if (inputPath == null &&
                file.getFileName() != null) {

            Path fallbackPath =
                    uploadDir
                            .resolve(file.getFileName())
                            .normalize();

            if (Files.exists(fallbackPath)) {
                inputPath = fallbackPath;
            }
        }

        // File not found
        if (inputPath == null) {

            throw new IOException(
                    "Word file not found.\n" +
                            "Database filename: " +
                            file.getFileName() + "\n" +
                            "Database path: " +
                            file.getFilePath() + "\n" +
                            "Upload folder: " +
                            uploadDir
            );
        }


        // ==========================================
        // 3. CREATE UPLOAD DIRECTORY
        // ==========================================

        Files.createDirectories(uploadDir);


        // ==========================================
        // 4. OUTPUT FILE NAME
        // ==========================================

        if (outputFileName == null ||
                outputFileName.isBlank()) {

            outputFileName = "converted.pdf";
        }

        outputFileName =
                Paths.get(outputFileName)
                        .getFileName()
                        .toString();

        if (!outputFileName
                .toLowerCase()
                .endsWith(".pdf")) {

            outputFileName += ".pdf";
        }


        // ==========================================
        // 5. OUTPUT PATH
        // ==========================================

        Path outputPath =
                uploadDir
                        .resolve(outputFileName)
                        .normalize();

        System.out.println("Input Word  : " + inputPath);
        System.out.println("Output PDF  : " + outputPath);


        // ==========================================
        // 6. READ WORD DOCUMENT
        // ==========================================

        try (
                FileInputStream inputStream =
                        new FileInputStream(
                                inputPath.toFile()
                        );

                XWPFDocument wordDocument =
                        new XWPFDocument(inputStream);

                PDDocument pdfDocument =
                        new PDDocument()
        ) {

            PDPage page = new PDPage();

            pdfDocument.addPage(page);

            PDPageContentStream contentStream =
                    new PDPageContentStream(
                            pdfDocument,
                            page
                    );

            contentStream.beginText();

            contentStream.setFont(
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    ),
                    12
            );

            contentStream.newLineAtOffset(
                    50,
                    750
            );


            // ==========================================
            // 7. CONVERT PARAGRAPHS
            // ==========================================

            for (XWPFParagraph paragraph :
                    wordDocument.getParagraphs()) {

                String text =
                        paragraph.getText();

                if (text == null ||
                        text.isBlank()) {

                    contentStream.newLineAtOffset(
                            0,
                            -15
                    );

                    continue;
                }

                contentStream.showText(
                        cleanText(text)
                );

                contentStream.newLineAtOffset(
                        0,
                        -18
                );
            }


            // ==========================================
            // 8. CLOSE PDF CONTENT
            // ==========================================

            contentStream.endText();
            contentStream.close();


            // ==========================================
            // 9. SAVE PDF
            // ==========================================

            pdfDocument.save(
                    outputPath.toFile()
            );
        }


        // ==========================================
        // 10. VERIFY PDF
        // ==========================================

        if (!Files.exists(outputPath)) {

            throw new IOException(
                    "PDF file was not created: " +
                            outputPath
            );
        }


        System.out.println("=================================");
        System.out.println("WORD TO PDF SUCCESS");
        System.out.println("PDF file: " + outputPath);
        System.out.println("=================================");


        // ==========================================
        // 11. SAVE PDF RECORD TO DATABASE
        // ==========================================

        PdfFile pdfFile =
                new PdfFile();

        pdfFile.setFileName(
                outputFileName
        );

        pdfFile.setFilePath(
                outputPath
                        .toAbsolutePath()
                        .toString()
        );

        pdfFile.setFileSize(
                Files.size(outputPath)
        );

        pdfRepository.save(pdfFile);


        return outputPath
                .toAbsolutePath()
                .toString();
    }


    // ==========================================
    // CLEAN TEXT FOR PDF
    // ==========================================

    private String cleanText(String text) {

        return text
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replaceAll("[^\\x20-\\x7E]", "");
    }
}