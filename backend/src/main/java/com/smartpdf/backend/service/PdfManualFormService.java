package com.smartpdf.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class PdfManualFormService {

    // =========================================================
    // ADD MULTIPLE FORM VALUES TO A PDF
    // =========================================================

    public String fillManualForm(
            String inputPath,
            List<FormField> fields,
            String outputFileName
    ) throws IOException {

        if (inputPath == null || inputPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Input PDF path cannot be empty"
            );
        }

        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException(
                    "No form fields were provided"
            );
        }

        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            throw new IOException(
                    "Input PDF does not exist: " + inputPath
            );
        }

        // -----------------------------------------------------
        // OUTPUT DIRECTORY
        // -----------------------------------------------------

        Path uploadDirectory =
                Paths.get(System.getProperty("user.dir"))
                        .resolve("uploads")
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(uploadDirectory);

        // -----------------------------------------------------
        // OUTPUT FILE NAME
        // -----------------------------------------------------

        if (outputFileName == null ||
                outputFileName.isBlank()) {

            outputFileName = "filled-form-result.pdf";
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

        Path outputPath =
                uploadDirectory
                        .resolve(outputFileName)
                        .normalize();

        // -----------------------------------------------------
        // PREVENT OVERWRITING INPUT
        // -----------------------------------------------------

        Path inputAbsolute =
                inputFile.toPath()
                        .toAbsolutePath()
                        .normalize();

        if (inputAbsolute.equals(
                outputPath.toAbsolutePath()
        )) {

            throw new IOException(
                    "Output file cannot be the same as input file"
            );
        }

        System.out.println("=================================");
        System.out.println("MANUAL PDF FORM FILLER");
        System.out.println("Input  : " + inputAbsolute);
        System.out.println("Output : " + outputPath);
        System.out.println("Fields : " + fields.size());
        System.out.println("=================================");

        // -----------------------------------------------------
        // LOAD PDF
        // -----------------------------------------------------

        try (PDDocument document =
                     Loader.loadPDF(inputFile)) {

            for (FormField field : fields) {

                if (field == null) {
                    continue;
                }

                int pageNumber =
                        field.getPageNumber();

                if (pageNumber < 1 ||
                        pageNumber >
                                document.getNumberOfPages()) {

                    throw new IllegalArgumentException(
                            "Invalid page number: "
                                    + pageNumber
                    );
                }

                PDPage page =
                        document.getPage(
                                pageNumber - 1
                        );

                System.out.println(
                        "Adding field: "
                                + field.getName()
                                + " on page "
                                + pageNumber
                );

                drawField(
                        document,
                        page,
                        field
                );
            }

            document.save(
                    outputPath.toFile()
            );
        }

        System.out.println("=================================");
        System.out.println("FORM FILL SUCCESS");
        System.out.println("Saved : " + outputPath);
        System.out.println("=================================");

        return outputPath.toAbsolutePath().toString();
    }


    // =========================================================
    // DRAW ONE FIELD
    // =========================================================

    private void drawField(
            PDDocument document,
            PDPage page,
            FormField field
    ) throws IOException {

        String value =
                field.getValue();

        if (value == null) {
            value = "";
        }

        value = cleanText(value);

        if (value.isEmpty()) {
            return;
        }

        float x =
                field.getX();

        float y =
                field.getY();

        float fontSize =
                field.getFontSize();

        if (fontSize <= 0) {
            fontSize = 12f;
        }

        PDFontWrapper fontWrapper =
                new PDFontWrapper();

        // -----------------------------------------------------
        // DRAW BACKGROUND
        // -----------------------------------------------------

        try (
                PDPageContentStream content =
                        new PDPageContentStream(
                                document,
                                page,
                                PDPageContentStream.AppendMode.APPEND,
                                true,
                                true
                        )
        ) {

            /*
             * White background makes the entered value
             * clearly visible on top of a static PDF.
             */

            float boxWidth =
                    field.getWidth() > 0
                            ? field.getWidth()
                            : 150f;

            float boxHeight =
                    field.getHeight() > 0
                            ? field.getHeight()
                            : fontSize + 6f;

            content.saveGraphicsState();

            content.setNonStrokingColor(
                    Color.WHITE
            );

            content.addRect(
                    x,
                    y,
                    boxWidth,
                    boxHeight
            );

            content.fill();

            content.restoreGraphicsState();

            // -------------------------------------------------
            // DRAW VALUE
            // -------------------------------------------------

            content.saveGraphicsState();

            /*
             * Dark text so that the new value is clearly
             * different from the original PDF content.
             */

            content.setNonStrokingColor(
                    new Color(
                            20,
                            40,
                            100
                    )
            );

            content.beginText();

            content.setFont(
                    fontWrapper.getFont(),
                    fontSize
            );

            content.newLineAtOffset(
                    x + 3f,
                    y + 3f
            );

            content.showText(value);

            content.endText();

            content.restoreGraphicsState();
        }
    }


    // =========================================================
    // CLEAN TEXT
    // =========================================================

    private String cleanText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ")
                .replace("\u0000", "")
                .trim();
    }


    // =========================================================
    // FORM FIELD
    // =========================================================

    public static class FormField {

        private String name;

        private String value;

        private int pageNumber;

        private float x;

        private float y;

        private float width;

        private float height;

        private float fontSize;


        public FormField() {
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }


        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }


        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }


        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }


        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }


        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }


        public float getFontSize() {
            return fontSize;
        }

        public void setFontSize(float fontSize) {
            this.fontSize = fontSize;
        }
    }


    // =========================================================
    // FONT WRAPPER
    // =========================================================

    private static class PDFontWrapper {

        private final PDType1Font font;

        PDFontWrapper() {

            font =
                    new PDType1Font(
                            Standard14Fonts.FontName.HELVETICA
                    );
        }

        public PDType1Font getFont() {
            return font;
        }
    }
}