
        package com.smartpdf.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ExcelToPdfService {

    private final Path uploadDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("uploads")
                    .toAbsolutePath()
                    .normalize();


    // =====================================================
    // EXCEL TO PDF
    // =====================================================

    public String convertExcelToPdf(
            String inputFileName,
            String outputFileName
    ) throws IOException {


        // -------------------------------------------------
        // CREATE UPLOAD DIRECTORY
        // -------------------------------------------------

        Files.createDirectories(uploadDir);


        // -------------------------------------------------
        // CLEAN FILE NAMES
        // -------------------------------------------------

        inputFileName =
                Paths.get(inputFileName)
                        .getFileName()
                        .toString();

        outputFileName =
                Paths.get(outputFileName)
                        .getFileName()
                        .toString();


        // -------------------------------------------------
        // DEFAULT OUTPUT NAME
        // -------------------------------------------------

        if (outputFileName.isBlank()) {
            outputFileName = "converted.pdf";
        }


        // -------------------------------------------------
        // MAKE SURE OUTPUT IS PDF
        // -------------------------------------------------

        if (!outputFileName
                .toLowerCase()
                .endsWith(".pdf")) {

            outputFileName += ".pdf";
        }


        // -------------------------------------------------
        // FILE PATHS
        // -------------------------------------------------

        Path inputPath =
                uploadDir
                        .resolve(inputFileName)
                        .normalize();

        Path outputPath =
                uploadDir
                        .resolve(outputFileName)
                        .normalize();


        // -------------------------------------------------
        // CHECK EXCEL FILE
        // -------------------------------------------------

        if (!Files.exists(inputPath)) {

            throw new IOException(
                    "Excel file does not exist: " +
                            inputPath
            );
        }


        System.out.println("=================================");
        System.out.println("EXCEL TO PDF");
        System.out.println("Excel : " + inputPath);
        System.out.println("PDF   : " + outputPath);
        System.out.println("=================================");


        // =================================================
        // READ EXCEL
        // =================================================

        try (
                InputStream inputStream =
                        Files.newInputStream(inputPath);

                Workbook workbook =
                        new XSSFWorkbook(inputStream);

                PDDocument document =
                        new PDDocument()
        ) {


            DataFormatter formatter =
                    new DataFormatter();


            // -------------------------------------------------
            // PROCESS FIRST SHEET
            // -------------------------------------------------

            Sheet sheet =
                    workbook.getSheetAt(0);


            // -------------------------------------------------
            // CREATE FIRST PDF PAGE
            // -------------------------------------------------

            PDPage page =
                    new PDPage(
                            PDRectangle.A4
                    );

            document.addPage(page);


            PDPageContentStream content =
                    new PDPageContentStream(
                            document,
                            page
                    );


            content.beginText();

            content.setFont(
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    ),
                    10
            );

            content.setLeading(15);

            content.newLineAtOffset(
                    40,
                    750
            );


            // =================================================
            // READ ROWS
            // =================================================

            for (Row row : sheet) {

                StringBuilder rowText =
                        new StringBuilder();


                // -------------------------------------------------
                // READ CELLS
                // -------------------------------------------------

                for (Cell cell : row) {

                    String value =
                            formatter.formatCellValue(
                                    cell
                            );

                    if (rowText.length() > 0) {
                        rowText.append("    ");
                    }

                    rowText.append(value);
                }


                // -------------------------------------------------
                // WRITE ROW TO PDF
                // -------------------------------------------------

                content.showText(
                        rowText.toString()
                );

                content.newLine();
            }


            content.endText();

            content.close();


            // -------------------------------------------------
            // SAVE PDF
            // -------------------------------------------------

            document.save(
                    outputPath.toFile()
            );
        }


        // =================================================
        // VERIFY
        // =================================================

        if (!Files.exists(outputPath)) {

            throw new IOException(
                    "PDF file was not created."
            );
        }


        System.out.println("=================================");
        System.out.println("EXCEL TO PDF SUCCESS");
        System.out.println("PDF : " + outputPath);
        System.out.println("=================================");


        return outputPath
                .toAbsolutePath()
                .toString();
    }
}
