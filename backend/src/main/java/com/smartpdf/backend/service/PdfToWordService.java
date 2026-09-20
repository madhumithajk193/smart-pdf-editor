package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PdfToWordService {

    @Autowired
    private PdfRepository pdfRepository;


    private final Path uploadDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("uploads")
                    .toAbsolutePath()
                    .normalize();


    public String convertPdfToWord(
            Long id,
            String outputFileName
    ) throws IOException {


        // =====================================================
        // 1. FIND PDF DATABASE RECORD
        // =====================================================

        PdfFile pdfFile = pdfRepository
                .findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "PDF database record not found. ID: " + id
                        )
                );


        System.out.println("=================================");
        System.out.println("PDF TO WORD");
        System.out.println("ID            : " + id);
        System.out.println("Database name : " + pdfFile.getFileName());
        System.out.println("Database path : " + pdfFile.getFilePath());
        System.out.println("Upload folder : " + uploadDir);
        System.out.println("=================================");


        // =====================================================
        // 2. GET DATABASE PATH
        // =====================================================

        Path inputPath =
                Paths.get(pdfFile.getFilePath())
                        .toAbsolutePath()
                        .normalize();


        // =====================================================
        // 3. CHECK DATABASE PATH
        // =====================================================

        if (!Files.exists(inputPath)) {

            System.out.println(
                    "Database path does NOT exist."
            );

            /*
             * Try finding the file using its database filename.
             */
            Path fallbackPath =
                    uploadDir.resolve(
                            pdfFile.getFileName()
                    ).normalize();


            System.out.println(
                    "Fallback path: " + fallbackPath
            );


            if (Files.exists(fallbackPath)) {

                inputPath = fallbackPath;

                System.out.println(
                        "Fallback file FOUND."
                );

            } else {

                throw new IOException(
                        "Source PDF file not found.\n" +
                                "Database path: " + inputPath + "\n" +
                                "Fallback path: " + fallbackPath + "\n" +
                                "Database file name: " +
                                pdfFile.getFileName()
                );
            }
        }


        // =====================================================
        // 4. CREATE UPLOAD DIRECTORY
        // =====================================================

        Files.createDirectories(uploadDir);


        // =====================================================
        // 5. VALIDATE OUTPUT NAME
        // =====================================================

        if (outputFileName == null ||
                outputFileName.isBlank()) {

            outputFileName = "converted.docx";
        }


        /*
         * Prevent directory traversal.
         */
        outputFileName =
                Paths.get(outputFileName)
                        .getFileName()
                        .toString();


        if (!outputFileName
                .toLowerCase()
                .endsWith(".docx")) {

            outputFileName += ".docx";
        }


        // =====================================================
        // 6. OUTPUT WORD PATH
        // =====================================================

        Path outputPath =
                uploadDir
                        .resolve(outputFileName)
                        .normalize();


        System.out.println(
                "Input PDF  : " + inputPath
        );

        System.out.println(
                "Output Word : " + outputPath
        );


        // =====================================================
        // 7. OPEN PDF
        // =====================================================

        try (
                PDDocument pdfDocument =
                        Loader.loadPDF(inputPath.toFile());

                XWPFDocument wordDocument =
                        new XWPFDocument()
        ) {


            // =================================================
            // 8. EXTRACT TEXT
            // =================================================

            PDFTextStripper stripper =
                    new PDFTextStripper();

            String text =
                    stripper.getText(pdfDocument);


            // =================================================
            // 9. CREATE WORD PARAGRAPH
            // =================================================

            XWPFParagraph paragraph =
                    wordDocument.createParagraph();


            paragraph
                    .createRun()
                    .setText(text);


            // =================================================
            // 10. SAVE WORD FILE
            // =================================================

            try (
                    FileOutputStream outputStream =
                            new FileOutputStream(
                                    outputPath.toFile()
                            )
            ) {

                wordDocument.write(outputStream);
            }
        }


        // =====================================================
        // 11. VERIFY OUTPUT
        // =====================================================

        if (!Files.exists(outputPath)) {

            throw new IOException(
                    "Word file was not created: "
                            + outputPath
            );
        }


        System.out.println("=================================");
        System.out.println("CONVERSION SUCCESS");
        System.out.println("Word file: " + outputPath);
        System.out.println("=================================");


        return outputPath
                .toAbsolutePath()
                .toString();
    }
}