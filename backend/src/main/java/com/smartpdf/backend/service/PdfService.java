package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

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
import org.apache.poi.ss.usermodel.WorkbookFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class PdfService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("uploads")
                    .toAbsolutePath()
                    .normalize();

    public PdfFile uploadPdf(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IOException("Please select a PDF, Word or Excel file");
        }

        // Create uploads directory
        Files.createDirectories(uploadDir);

        // Get original filename
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IOException("Invalid file name");
        }

        // Remove directory information
        originalFileName = Paths.get(originalFileName)
                .getFileName()
                .toString();

        String lowerFileName = originalFileName.toLowerCase();

        // Check extension
        boolean isPdf = lowerFileName.endsWith(".pdf");
        boolean isDoc = lowerFileName.endsWith(".doc");
        boolean isDocx = lowerFileName.endsWith(".docx");
        boolean isXlsx = lowerFileName.endsWith(".xlsx");
        boolean isXls = lowerFileName.endsWith(".xls");

        if (!isPdf && !isDoc && !isDocx && !isXlsx && !isXls) {
            throw new IOException(
                    "Only PDF, DOC, DOCX, XLS and XLSX files are allowed"
            );
        }

        /*
         * IMPORTANT:
         * Create a unique stored filename.
         *
         * Example:
         * employees.pdf
         * becomes
         * 8f4a7c2e-employees.pdf
         */
        String uniqueFileName =
                java.util.UUID.randomUUID() + "_" + originalFileName;

        Path filePath = uploadDir
                .resolve(uniqueFileName)
                .normalize();

        // Save uploaded file
        try (java.io.InputStream inputStream = file.getInputStream()) {

            Files.copy(
                    inputStream,
                    filePath
            );
        }

        System.out.println("=================================");
        System.out.println("FILE UPLOAD SUCCESS");
        System.out.println("Original name : " + originalFileName);
        System.out.println("Stored name   : " + uniqueFileName);
        System.out.println("File path     : " + filePath);
        System.out.println("File size     : " + file.getSize());
        System.out.println("Exists        : " + Files.exists(filePath));
        System.out.println("=================================");

        // Save information in database
        PdfFile pdf = new PdfFile();

        // Keep original filename for displaying to user
        pdf.setFileName(originalFileName);

        // Store actual unique file path
        pdf.setFilePath(filePath.toString());

        pdf.setFileSize(file.getSize());

        return pdfRepository.save(pdf);
    }


    // =========================================================
    // 2. GET ALL PDFs
    // =========================================================

    public List<PdfFile> getAllPdfs() {
        return pdfRepository.findAll();
    }

    // =========================================================
    // 3. GET PDF BY ID
    // =========================================================
    public PdfFile getPdfById(Long id) {

        System.out.println("Looking for PDF ID: " + id);

        PdfFile pdf = pdfRepository
                .findById(id)
                .orElse(null);

        if (pdf == null) {

            System.out.println(
                    "❌ PDF ID NOT FOUND IN DATABASE: " + id
            );

        } else {

            System.out.println(
                    "✅ PDF FOUND"
            );

            System.out.println(
                    "File name: " + pdf.getFileName()
            );

            System.out.println(
                    "File path: " + pdf.getFilePath()
            );
        }

        return pdf;
    }

    // =========================================================
    // 4. DELETE PDF
    // =========================================================

    public void deletePdf(Long id) throws IOException {

        PdfFile pdf = pdfRepository
                .findById(id)
                .orElse(null);

        if (pdf == null) {
            throw new IOException("PDF not found");
        }

        Path filePath =
                Paths.get(pdf.getFilePath());

        Files.deleteIfExists(filePath);

        pdfRepository.deleteById(id);
    }

    // =========================================================
    // 5. EXCEL TO PDF
    // =========================================================

    public byte[] convertExcelToPdf(MultipartFile file)
            throws IOException {

        // -----------------------------------------------------
        // Check file
        // -----------------------------------------------------

        if (file == null || file.isEmpty()) {
            throw new IOException(
                    "Please select an Excel file"
            );
        }

        String originalFileName =
                file.getOriginalFilename();

        if (originalFileName == null ||
                originalFileName.isBlank()) {

            throw new IOException(
                    "Invalid Excel file name"
            );
        }

        // -----------------------------------------------------
        // Check extension
        // -----------------------------------------------------

        String lowerName =
                originalFileName.toLowerCase();

        if (!lowerName.endsWith(".xlsx") &&
                !lowerName.endsWith(".xls")) {

            throw new IOException(
                    "Only .xlsx and .xls files are allowed"
            );
        }

        System.out.println("=================================");
        System.out.println("EXCEL TO PDF");
        System.out.println("File       : " + originalFileName);
        System.out.println("Size       : " + file.getSize());
        System.out.println("=================================");

        // -----------------------------------------------------
        // Create Excel workbook
        // -----------------------------------------------------

        try (
                Workbook workbook =
                        WorkbookFactory.create(
                                file.getInputStream()
                        );

                PDDocument document =
                        new PDDocument();

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            DataFormatter formatter =
                    new DataFormatter();

            PDType1Font font =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    );

            PDType1Font boldFont =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA_BOLD
                    );

            float margin = 40;

            float pageWidth =
                    PDRectangle.A4.getWidth();

            float pageHeight =
                    PDRectangle.A4.getHeight();

            float usableWidth =
                    pageWidth - (margin * 2);

            float rowHeight = 18;

            float fontSize = 8;

            // -------------------------------------------------
            // Process every Excel sheet
            // -------------------------------------------------

            for (int sheetIndex = 0;
                 sheetIndex < workbook.getNumberOfSheets();
                 sheetIndex++) {

                Sheet sheet =
                        workbook.getSheetAt(sheetIndex);

                System.out.println(
                        "Processing sheet: "
                                + sheet.getSheetName()
                );

                // -------------------------------------------------
                // Empty sheet
                // -------------------------------------------------

                if (sheet.getPhysicalNumberOfRows() == 0) {

                    PDPage page =
                            new PDPage(PDRectangle.A4);

                    document.addPage(page);

                    try (
                            PDPageContentStream content =
                                    new PDPageContentStream(
                                            document,
                                            page
                                    )
                    ) {

                        content.beginText();

                        content.setFont(
                                boldFont,
                                12
                        );

                        content.newLineAtOffset(
                                margin,
                                pageHeight - margin
                        );

                        content.showText(
                                cleanText(
                                        sheet.getSheetName()
                                )
                        );

                        content.endText();
                    }

                    continue;
                }

                // -------------------------------------------------
                // Find maximum columns
                // -------------------------------------------------

                int maxColumns = 0;

                for (Row row : sheet) {

                    if (row.getLastCellNum() > maxColumns) {
                        maxColumns =
                                row.getLastCellNum();
                    }
                }

                if (maxColumns <= 0) {
                    maxColumns = 1;
                }

                // -------------------------------------------------
                // Calculate column width
                // -------------------------------------------------

                float columnWidth =
                        usableWidth / maxColumns;

                // Prevent columns from becoming too wide
                if (columnWidth > 120) {
                    columnWidth = 120;
                }

                // -------------------------------------------------
                // Create first page
                // -------------------------------------------------

                PDPage page =
                        new PDPage(PDRectangle.A4);

                document.addPage(page);

                PDPageContentStream content =
                        new PDPageContentStream(
                                document,
                                page
                        );

                float y =
                        pageHeight - margin;

                // -------------------------------------------------
                // Sheet title
                // -------------------------------------------------

                content.beginText();

                content.setFont(
                        boldFont,
                        12
                );

                content.newLineAtOffset(
                        margin,
                        y
                );

                content.showText(
                        cleanText(
                                sheet.getSheetName()
                        )
                );

                content.endText();

                y -= 25;

                // -------------------------------------------------
                // Process rows
                // -------------------------------------------------

                for (Row row : sheet) {

                    // New page if required
                    if (y < margin + rowHeight) {

                        content.close();

                        page =
                                new PDPage(
                                        PDRectangle.A4
                                );

                        document.addPage(page);

                        content =
                                new PDPageContentStream(
                                        document,
                                        page
                                );

                        y =
                                pageHeight - margin;
                    }

                    // -------------------------------------------------
                    // Process cells
                    // -------------------------------------------------

                    for (int column = 0;
                         column < maxColumns;
                         column++) {

                        Cell cell =
                                row.getCell(
                                        column,
                                        Row.MissingCellPolicy
                                                .RETURN_BLANK_AS_NULL
                                );

                        String value = "";

                        if (cell != null) {

                            value =
                                    formatter.formatCellValue(
                                            cell
                                    );
                        }

                        value =
                                cleanText(value);

                        // Limit text length
                        if (value.length() > 20) {
                            value =
                                    value.substring(
                                            0,
                                            20
                                    );
                        }

                        float x =
                                margin
                                        + (column * columnWidth);

                        // -------------------------------------------------
                        // Cell border
                        // -------------------------------------------------

                        content.addRect(
                                x,
                                y - rowHeight,
                                columnWidth,
                                rowHeight
                        );

                        content.stroke();

                        // -------------------------------------------------
                        // Cell text
                        // -------------------------------------------------

                        if (!value.isEmpty()) {

                            content.beginText();

                            // First row = bold
                            if (row.getRowNum() == 0) {

                                content.setFont(
                                        boldFont,
                                        fontSize
                                );

                            } else {

                                content.setFont(
                                        font,
                                        fontSize
                                );
                            }

                            content.newLineAtOffset(
                                    x + 3,
                                    y - 13
                            );

                            content.showText(
                                    value
                            );

                            content.endText();
                        }
                    }

                    y -= rowHeight;
                }

                content.close();
            }

            // -----------------------------------------------------
            // Save PDF into memory
            // -----------------------------------------------------

            document.save(output);

            byte[] pdfBytes =
                    output.toByteArray();

            System.out.println("=================================");
            System.out.println("EXCEL TO PDF SUCCESS");
            System.out.println("PDF size : " + pdfBytes.length);
            System.out.println("=================================");

            return pdfBytes;

        } catch (Exception e) {

            System.out.println("=================================");
            System.out.println("EXCEL TO PDF ERROR");
            System.out.println("Message : " + e.getMessage());
            System.out.println("=================================");

            e.printStackTrace();

            if (e instanceof IOException) {
                throw (IOException) e;
            }

            throw new IOException(
                    "Excel conversion failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    // =========================================================
    // 6. CLEAN TEXT FOR PDF
    // =========================================================

    private String cleanText(String text) {

        if (text == null) {
            return "";
        }

        // PDFBox Helvetica cannot handle all Unicode
        // characters, so replace unsupported characters.

        return text
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replace("\u0000", "")
                .replaceAll(
                        "[^\\x20-\\x7E]",
                        "?"
                )
                .trim();
    }
}