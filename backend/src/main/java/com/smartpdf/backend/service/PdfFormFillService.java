package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.CustomFormField;
import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.CustomFormFieldRepository;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class PdfFormFillService {

    @Autowired
    private PdfRepository pdfRepository;

    @Autowired
    private CustomFormFieldRepository customFormFieldRepository;


    // =========================================================
    // BACKWARD COMPATIBILITY
    // =========================================================

    public String fillPdf(
            Long pdfId,
            List<CustomFormField> fields,
            String outputFileName
    ) throws IOException {

        return fillPdf(
                pdfId,
                fields,
                null,
                outputFileName
        );
    }


    // =========================================================
    // MAIN PDF FILL METHOD
    //
    // IMPORTANT ORDER:
    //
    // pdfId
    // fields
    // signatureImage
    // outputFileName
    //
    // This MUST match your controller.
    // =========================================================

    public String fillPdf(
            Long pdfId,
            List<CustomFormField> fields,
            String signatureImage,
            String outputFileName
    ) throws IOException {

        // =====================================================
        // VALIDATION
        // =====================================================

        if (pdfId == null) {

            throw new IllegalArgumentException(
                    "PDF ID cannot be null"
            );
        }

        if (fields == null) {

            fields = new ArrayList<>();
        }


        // =====================================================
        // FIND PDF
        // =====================================================

        PdfFile pdfFile =
                pdfRepository.findById(pdfId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "PDF not found with ID: "
                                                + pdfId
                                )
                        );


        // =====================================================
        // INPUT FILE
        // =====================================================

        if (pdfFile.getFilePath() == null ||
                pdfFile.getFilePath().isBlank()) {

            throw new IOException(
                    "PDF file path is missing"
            );
        }

        File inputFile =
                new File(
                        pdfFile.getFilePath()
                );

        if (!inputFile.exists()) {

            throw new IOException(
                    "PDF file does not exist: "
                            + pdfFile.getFilePath()
            );
        }


        // =====================================================
        // OUTPUT DIRECTORY
        // =====================================================

        Path uploadDirectory =
                Paths.get(
                                System.getProperty("user.dir"),
                                "uploads"
                        )
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(
                uploadDirectory
        );


        // =====================================================
        // OUTPUT FILE NAME
        // =====================================================

        if (outputFileName == null ||
                outputFileName.isBlank()) {

            outputFileName =
                    "filled-" +
                            pdfFile.getFileName();
        }

        /*
         * IMPORTANT:
         *
         * At this point outputFileName must contain
         * something like:
         *
         * employee-filled.pdf
         *
         * It must NOT contain the Base64 signature.
         */

        outputFileName =
                Paths.get(
                                outputFileName
                        )
                        .getFileName()
                        .toString();

        if (!outputFileName
                .toLowerCase(Locale.ROOT)
                .endsWith(".pdf")) {

            outputFileName += ".pdf";
        }


        // =====================================================
        // OUTPUT PATH
        // =====================================================

        Path outputPath =
                uploadDirectory
                        .resolve(outputFileName)
                        .normalize();


        if (!outputPath
                .getParent()
                .equals(uploadDirectory)) {

            throw new IOException(
                    "Invalid output file name"
            );
        }


        // =====================================================
        // LOAD PDF
        // =====================================================

        try (
                PDDocument document =
                        Loader.loadPDF(inputFile)
        ) {

            System.out.println(
                    "========================================"
            );

            System.out.println(
                    "PDF FORM FILLER"
            );

            System.out.println(
                    "PDF: " +
                            pdfFile.getFileName()
            );

            System.out.println(
                    "Pages: " +
                            document.getNumberOfPages()
            );

            System.out.println(
                    "Fields: " +
                            fields.size()
            );

            System.out.println(
                    "Signature: " +
                            (
                                    signatureImage != null &&
                                            !signatureImage.isBlank()
                                            ? "YES"
                                            : "NO"
                            )
            );

            System.out.println(
                    "========================================"
            );


            // =================================================
            // EXTRACT PDF TEXT POSITIONS
            // =================================================

            Map<Integer, List<TextPosition>> pageText =
                    extractTextPositions(
                            document
                    );


            // =================================================
            // PROCESS ALL FORM FIELDS
            // =================================================

            for (CustomFormField field : fields) {

                if (field == null) {
                    continue;
                }

                String value =
                        field.getFieldValue();

                if (value == null ||
                        value.isBlank()) {

                    continue;
                }


                // -------------------------------------------------
                // PAGE NUMBER
                // -------------------------------------------------

                int targetPage = 0;

                Integer pageNumber =
                        field.getPageNumber();

                if (pageNumber != null &&
                        pageNumber >= 1 &&
                        pageNumber <=
                                document.getNumberOfPages()) {

                    targetPage =
                            pageNumber - 1;
                }
                else {

                    targetPage =
                            findFieldPage(
                                    field,
                                    pageText
                            );
                }


                if (targetPage < 0 ||
                        targetPage >=
                                document.getNumberOfPages()) {

                    targetPage = 0;
                }


                PDPage page =
                        document.getPage(
                                targetPage
                        );


                // -------------------------------------------------
                // FIELD NAME
                // -------------------------------------------------

                String fieldName =
                        field.getFieldName();

                if (fieldName == null) {
                    continue;
                }


                // -------------------------------------------------
                // FIND THE ACTUAL LABEL LINE
                // -------------------------------------------------

                LabelInfo label =
                        findLabelPosition(
                                pageText.get(
                                        targetPage
                                ),
                                fieldName
                        );


                // -------------------------------------------------
                // AUTOMATIC POSITION
                // -------------------------------------------------

                if (label != null) {

                    System.out.println(
                            "Field: " +
                                    fieldName +
                                    " -> label found at (" +
                                    label.x +
                                    ", " +
                                    label.y +
                                    ")"
                    );

                    drawValueBesideLabel(
                            document,
                            page,
                            label,
                            value
                    );
                }
                else {

                    System.out.println(
                            "Label not found for: " +
                                    fieldName +
                                    " -> using saved coordinates"
                    );

                    drawFieldValue(
                            document,
                            page,
                            field,
                            value
                    );
                }
            }


            // =================================================
            // SIGNATURE
            // =================================================

            if (signatureImage != null &&
                    !signatureImage.isBlank()) {

                placeSignature(
                        document,
                        pageText,
                        signatureImage
                );
            }


            // =================================================
            // SAVE
            // =================================================

            document.save(
                    outputPath.toFile()
            );
        }


        System.out.println(
                "Filled PDF saved: " +
                        outputPath
        );

        return outputPath
                .toAbsolutePath()
                .toString();
    }


    // =========================================================
    // EXTRACT TEXT POSITIONS
    // =========================================================

    private Map<Integer, List<TextPosition>>
    extractTextPositions(
            PDDocument document
    ) throws IOException {

        PositionExtractor extractor =
                new PositionExtractor();

        extractor.setSortByPosition(
                true
        );

        extractor.getText(
                document
        );

        return extractor.getPositions();
    }


    // =========================================================
    // FIND LABEL
    //
    // IMPORTANT:
    //
    // Do NOT compare individual characters.
    //
    // We first combine characters into text lines.
    // =========================================================

    private LabelInfo findLabelPosition(
            List<TextPosition> positions,
            String fieldName
    ) {

        if (positions == null ||
                positions.isEmpty() ||
                fieldName == null) {

            return null;
        }


        String wanted =
                normalize(
                        fieldName
                );


        if (wanted.isEmpty()) {
            return null;
        }


        List<LabelInfo> lines =
                buildTextLines(
                        positions
                );


        // =====================================================
        // EXACT MATCH
        // =====================================================

        for (LabelInfo line : lines) {

            String lineText =
                    normalize(
                            line.text
                    );

            if (lineText.equals(wanted)) {

                return line;
            }
        }


        // =====================================================
        // ALTERNATIVE LABEL
        // =====================================================

        String alternative =
                getAlternativeLabel(
                        fieldName
                );

        if (alternative != null) {

            String normalizedAlternative =
                    normalize(
                            alternative
                    );

            for (LabelInfo line : lines) {

                String lineText =
                        normalize(
                                line.text
                        );

                if (lineText.equals(
                        normalizedAlternative
                )) {

                    return line;
                }
            }
        }


        // =====================================================
        // CONTAINS MATCH
        //
        // Only use complete line matching.
        // This prevents the previous problem where
        // one single character was selected.
        // =====================================================

        for (LabelInfo line : lines) {

            String lineText =
                    normalize(
                            line.text
                    );

            if (lineText.contains(wanted) ||
                    wanted.contains(lineText)) {

                if (lineText.length() >= 4) {

                    return line;
                }
            }
        }


        return null;
    }


    // =========================================================
    // BUILD TEXT LINES
    // =========================================================

    private List<LabelInfo> buildTextLines(
            List<TextPosition> positions
    ) {

        List<LabelInfo> result =
                new ArrayList<>();

        if (positions == null ||
                positions.isEmpty()) {

            return result;
        }


        List<TextPosition> sorted =
                new ArrayList<>(
                        positions
                );


        // -----------------------------------------------------
        // SORT BY Y THEN X
        // -----------------------------------------------------

        sorted.sort(
                Comparator
                        .comparingDouble(
                                TextPosition::getYDirAdj
                        )
                        .thenComparingDouble(
                                TextPosition::getXDirAdj
                        )
        );


        List<TextPosition> currentLine =
                new ArrayList<>();


        float currentY =
                -10000f;


        // -----------------------------------------------------
        // GROUP CHARACTERS THAT ARE ON THE SAME LINE
        // -----------------------------------------------------

        for (TextPosition position :
                sorted) {

            float y =
                    position.getYDirAdj();


            if (currentLine.isEmpty()) {

                currentLine.add(
                        position
                );

                currentY = y;

                continue;
            }


            if (Math.abs(
                    y - currentY
            ) <= 3.0f) {

                currentLine.add(
                        position
                );
            }
            else {

                LabelInfo line =
                        createLabelInfo(
                                currentLine
                        );

                if (line != null) {

                    result.add(
                            line
                    );
                }


                currentLine =
                        new ArrayList<>();

                currentLine.add(
                        position
                );

                currentY = y;
            }
        }


        // -----------------------------------------------------
        // LAST LINE
        // -----------------------------------------------------

        if (!currentLine.isEmpty()) {

            LabelInfo line =
                    createLabelInfo(
                            currentLine
                    );

            if (line != null) {

                result.add(
                        line
                );
            }
        }


        return result;
    }


    // =========================================================
    // CREATE LINE INFORMATION
    // =========================================================

    private LabelInfo createLabelInfo(
            List<TextPosition> positions
    ) {

        if (positions == null ||
                positions.isEmpty()) {

            return null;
        }


        positions.sort(
                Comparator.comparingDouble(
                        TextPosition::getXDirAdj
                )
        );


        StringBuilder text =
                new StringBuilder();


        float minX =
                Float.MAX_VALUE;

        float maxX =
                Float.MIN_VALUE;

        float minY =
                Float.MAX_VALUE;

        float maxY =
                Float.MIN_VALUE;


        for (TextPosition position :
                positions) {

            String unicode =
                    position.getUnicode();

            if (unicode != null) {

                text.append(
                        unicode
                );
            }


            float x =
                    position.getXDirAdj();

            float y =
                    position.getYDirAdj();

            float width =
                    position.getWidthDirAdj();

            float height =
                    position.getHeightDir();


            minX =
                    Math.min(
                            minX,
                            x
                    );

            maxX =
                    Math.max(
                            maxX,
                            x + width
                    );

            minY =
                    Math.min(
                            minY,
                            y
                    );

            maxY =
                    Math.max(
                            maxY,
                            y + height
                    );
        }


        String clean =
                text.toString()
                        .trim();


        if (clean.isEmpty()) {

            return null;
        }


        LabelInfo info =
                new LabelInfo();

        info.text = clean;

        info.x = minX;

        info.y = minY;

        info.width =
                maxX - minX;

        info.height =
                Math.max(
                        10f,
                        maxY - minY
                );

        return info;
    }


    // =========================================================
    // ALTERNATIVE LABEL
    // =========================================================

    private String getAlternativeLabel(
            String fieldName
    ) {

        if (fieldName == null) {
            return null;
        }


        String name =
                normalize(
                        fieldName
                );


        if (name.contains("employee") &&
                name.contains("name")) {

            return "Employee Name";
        }


        if (name.contains("employee") &&
                name.contains("id")) {

            return "Employee ID";
        }


        if (name.contains("department")) {

            return "Department";
        }


        if (name.contains("email")) {

            return "Email";
        }


        if (name.contains("phone") ||
                name.contains("mobile")) {

            return "Phone Number";
        }


        if (name.contains("joining") &&
                name.contains("date")) {

            return "Joining Date";
        }


        if (name.contains("address")) {

            return "Address";
        }


        if (name.contains("employment") &&
                name.contains("type")) {

            return "Employment Type";
        }


        if (name.contains("signature")) {

            return "Employee Signature";
        }


        return null;
    }


    // =========================================================
    // DRAW VALUE BESIDE LABEL
    // =========================================================

    private void drawValueBesideLabel(
            PDDocument document,
            PDPage page,
            LabelInfo label,
            String value
    ) throws IOException {


        PDRectangle mediaBox =
                page.getMediaBox();


        float pageWidth =
                mediaBox.getWidth();

        float pageHeight =
                mediaBox.getHeight();


        // =====================================================
        // POSITION
        // =====================================================

        /*
         * The employee form has labels on the left and
         * empty boxes/lines on the right.
         *
         * Put the value after the complete label,
         * NOT after the first character.
         */

        float x =
                label.x +
                        label.width +
                        18f;


        float topY =
                label.y;


        // =====================================================
        // WIDTH
        // =====================================================

        float availableWidth =
                pageWidth -
                        x -
                        25f;


        if (availableWidth < 60f) {

            x =
                    label.x;

            topY =
                    label.y +
                            label.height +
                            3f;

            availableWidth =
                    pageWidth -
                            x -
                            25f;
        }


        // =====================================================
        // FONT
        // =====================================================

        PDType1Font font =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );


        float fontSize =
                Math.min(
                        11f,
                        Math.max(
                                9f,
                                label.height
                        )
                );


        // =====================================================
        // TEXT
        // =====================================================

        String cleanValue =
                cleanText(
                        value
                );


        cleanValue =
                fitText(
                        font,
                        cleanValue,
                        fontSize,
                        availableWidth
                );


        // =====================================================
        // PDF COORDINATE
        // =====================================================

        float pdfY =
                pageHeight -
                        topY -
                        label.height;


        // =====================================================
        // DRAW
        // =====================================================

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

            content.saveGraphicsState();


            // -------------------------------------------------
            // WHITE BACKGROUND
            // -------------------------------------------------

            content.setNonStrokingColor(
                    Color.WHITE
            );


            content.addRect(
                    x - 2f,
                    pdfY - 2f,
                    availableWidth,
                    label.height + 5f
            );


            content.fill();


            content.restoreGraphicsState();


            // -------------------------------------------------
            // TEXT
            // -------------------------------------------------

            content.beginText();


            content.setFont(
                    font,
                    fontSize
            );


            content.setNonStrokingColor(
                    Color.BLACK
            );


            content.newLineAtOffset(
                    x,
                    pdfY + 2f
            );


            content.showText(
                    cleanValue
            );


            content.endText();
        }
    }


    // =========================================================
    // FALLBACK FIELD VALUE
    // =========================================================

    private void drawFieldValue(
            PDDocument document,
            PDPage page,
            CustomFormField field,
            String value
    ) throws IOException {

        Float fieldX =
                field.getX();

        Float fieldY =
                field.getY();

        Float fieldWidth =
                field.getWidth();

        Float fieldHeight =
                field.getHeight();


        if (fieldX == null ||
                fieldY == null ||
                fieldWidth == null ||
                fieldHeight == null) {

            return;
        }


        float x =
                fieldX;

        float topY =
                fieldY;

        float width =
                fieldWidth;

        float height =
                fieldHeight;


        PDType1Font font =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );


        float fontSize =
                Math.max(
                        8f,
                        Math.min(
                                13f,
                                height * 0.65f
                        )
                );


        String cleanValue =
                cleanText(
                        value
                );


        cleanValue =
                fitText(
                        font,
                        cleanValue,
                        fontSize,
                        Math.max(
                                10f,
                                width - 6f
                        )
                );


        float pageHeight =
                page.getMediaBox()
                        .getHeight();


        float pdfY =
                pageHeight -
                        topY -
                        height;


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

            content.saveGraphicsState();


            content.setNonStrokingColor(
                    Color.WHITE
            );


            content.addRect(
                    x,
                    pdfY,
                    width,
                    height
            );


            content.fill();


            content.restoreGraphicsState();


            content.beginText();


            content.setFont(
                    font,
                    fontSize
            );


            content.setNonStrokingColor(
                    Color.BLACK
            );


            float textY =
                    pdfY +
                            (height - fontSize) /
                                    2f +
                            2f;


            content.newLineAtOffset(
                    x + 3f,
                    textY
            );


            content.showText(
                    cleanValue
            );


            content.endText();
        }
    }


    // =========================================================
    // PLACE SIGNATURE
    // =========================================================

    private void placeSignature(
            PDDocument document,
            Map<Integer, List<TextPosition>> pageText,
            String signatureImage
    ) throws IOException {


        int signaturePage =
                -1;


        LabelInfo signatureLabel =
                null;


        // =====================================================
        // FIND EMPLOYEE SIGNATURE
        // =====================================================

        for (
                Map.Entry<Integer,
                        List<TextPosition>> entry :
                pageText.entrySet()
        ) {

            LabelInfo found =
                    findLabelPosition(
                            entry.getValue(),
                            "Employee Signature"
                    );


            if (found != null) {

                signaturePage =
                        entry.getKey();

                signatureLabel =
                        found;

                break;
            }
        }


        // =====================================================
        // TRY "SIGNATURE"
        // =====================================================

        if (signatureLabel == null) {

            for (
                    Map.Entry<Integer,
                            List<TextPosition>> entry :
                    pageText.entrySet()
            ) {

                LabelInfo found =
                        findLabelPosition(
                                entry.getValue(),
                                "Signature"
                        );


                if (found != null) {

                    signaturePage =
                            entry.getKey();

                    signatureLabel =
                            found;

                    break;
                }
            }
        }


        // =====================================================
        // NO LABEL
        // =====================================================

        if (signatureLabel == null) {

            System.out.println(
                    "Employee Signature label not found."
            );

            return;
        }


        PDPage page =
                document.getPage(
                        signaturePage
                );


        // =====================================================
        // SIGNATURE POSITION
        // =====================================================

        float x =
                signatureLabel.x +
                        signatureLabel.width +
                        15f;


        float topY =
                signatureLabel.y;


        float pageWidth =
                page.getMediaBox()
                        .getWidth();

        float pageHeight =
                page.getMediaBox()
                        .getHeight();


        // =====================================================
        // SIGNATURE SIZE
        // =====================================================

        float signatureWidth =
                115f;

        float signatureHeight =
                35f;


        // =====================================================
        // KEEP INSIDE PAGE
        // =====================================================

        if (
                x + signatureWidth >
                        pageWidth - 15f
        ) {

            x =
                    signatureLabel.x;

            topY =
                    signatureLabel.y +
                            signatureLabel.height +
                            5f;
        }


        // =====================================================
        // CREATE IMAGE
        // =====================================================

        PDImageXObject image =
                createSignatureImage(
                        document,
                        signatureImage
                );


        if (image == null) {

            System.out.println(
                    "Signature image could not be created."
            );

            return;
        }


        // =====================================================
        // PDF Y
        // =====================================================

        float pdfY =
                pageHeight -
                        topY -
                        signatureHeight;


        // =====================================================
        // DRAW SIGNATURE
        // =====================================================

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

            content.saveGraphicsState();


            // -------------------------------------------------
            // WHITE BACKGROUND
            // -------------------------------------------------

            content.setNonStrokingColor(
                    Color.WHITE
            );


            content.addRect(
                    x - 2f,
                    pdfY - 2f,
                    signatureWidth + 4f,
                    signatureHeight + 4f
            );


            content.fill();


            // -------------------------------------------------
            // SIGNATURE IMAGE
            // -------------------------------------------------

            content.drawImage(
                    image,
                    x,
                    pdfY,
                    signatureWidth,
                    signatureHeight
            );


            content.restoreGraphicsState();
        }


        System.out.println(
                "Signature placed successfully."
        );
    }


    // =========================================================
    // CREATE SIGNATURE IMAGE
    // =========================================================

    private PDImageXObject createSignatureImage(
            PDDocument document,
            String signatureImage
    ) throws IOException {


        if (signatureImage == null ||
                signatureImage.isBlank()) {

            return null;
        }


        try {

            String base64 =
                    signatureImage.trim();


            // =================================================
            // HANDLE DATA URL
            //
            // Example:
            //
            // data:image/png;base64,iVBORw0...
            //
            // =================================================

            if (base64.startsWith(
                    "data:"
            )) {

                int comma =
                        base64.indexOf(",");


                if (comma >= 0) {

                    base64 =
                            base64.substring(
                                    comma + 1
                            );
                }
            }


            // =================================================
            // REMOVE WHITESPACE
            // =================================================

            base64 =
                    base64
                            .replace(
                                    "\n",
                                    ""
                            )
                            .replace(
                                    "\r",
                                    ""
                            )
                            .replace(
                                    " ",
                                    ""
                            )
                            .trim();


            // =================================================
            // DECODE
            // =================================================

            byte[] imageBytes =
                    Base64.getDecoder()
                            .decode(
                                    base64
                            );


            if (imageBytes.length == 0) {

                throw new IOException(
                        "Signature image is empty"
                );
            }


            // =================================================
            // CREATE PDF IMAGE
            // =================================================

            return PDImageXObject.createFromByteArray(
                    document,
                    imageBytes,
                    "employee-signature.png"
            );

        }
        catch (IllegalArgumentException e) {

            throw new IOException(
                    "Invalid Base64 signature image",
                    e
            );
        }
    }


    // =========================================================
    // FIND FIELD PAGE
    // =========================================================

    private int findFieldPage(
            CustomFormField field,
            Map<Integer, List<TextPosition>> pageText
    ) {

        if (field == null ||
                field.getFieldName() == null) {

            return 0;
        }


        String fieldName =
                field.getFieldName();


        for (
                Map.Entry<Integer,
                        List<TextPosition>> entry :
                pageText.entrySet()
        ) {

            if (
                    findLabelPosition(
                            entry.getValue(),
                            fieldName
                    ) != null
            ) {

                return entry.getKey();
            }
        }


        return 0;
    }


    // =========================================================
    // FIT TEXT
    // =========================================================

    private String fitText(
            PDType1Font font,
            String text,
            float fontSize,
            float availableWidth
    ) throws IOException {

        if (text == null) {
            return "";
        }


        if (availableWidth <= 0) {
            return text;
        }


        float textWidth =
                font.getStringWidth(
                        text
                )
                        / 1000f
                        * fontSize;


        if (textWidth <=
                availableWidth) {

            return text;
        }


        StringBuilder result =
                new StringBuilder();


        for (
                int i = 0;
                i < text.length();
                i++
        ) {

            String candidate =
                    result.toString() +
                            text.charAt(i);


            float candidateWidth =
                    font.getStringWidth(
                            candidate
                    )
                            / 1000f
                            * fontSize;


            if (
                    candidateWidth >
                            availableWidth
            ) {

                break;
            }


            result.append(
                    text.charAt(i)
            );
        }


        return result
                .toString()
                .trim();
    }


    // =========================================================
    // NORMALIZE TEXT
    // =========================================================

    private String normalize(
            String text
    ) {

        if (text == null) {
            return "";
        }


        return text
                .toLowerCase(
                        Locale.ROOT
                )
                .replaceAll(
                        "[^a-z0-9]",
                        ""
                )
                .trim();
    }


    // =========================================================
    // CLEAN TEXT
    // =========================================================

    private String cleanText(
            String text
    ) {

        if (text == null) {
            return "";
        }


        return text
                .replace(
                        "\n",
                        " "
                )
                .replace(
                        "\r",
                        " "
                )
                .replace(
                        "\t",
                        " "
                )
                .replace(
                        "\u0000",
                        ""
                )
                .trim();
    }


    // =========================================================
    // LABEL INFO
    // =========================================================

    private static class LabelInfo {

        String text;

        float x;

        float y;

        float width;

        float height;
    }


    // =========================================================
    // TEXT POSITION EXTRACTOR
    // =========================================================

    private static class PositionExtractor
            extends PDFTextStripper {

        private final Map<
                Integer,
                List<TextPosition>
                > positions =
                new HashMap<>();


        public PositionExtractor()
                throws IOException {

            super();
        }


        @Override
        protected void processTextPosition(
                TextPosition text
        ) {

            int pageNumber =
                    getCurrentPageNo() - 1;


            positions
                    .computeIfAbsent(
                            pageNumber,
                            k ->
                                    new ArrayList<>()
                    )
                    .add(
                            text
                    );


            super.processTextPosition(
                    text
            );
        }


        public Map<
                Integer,
                List<TextPosition>
                >
        getPositions() {

            return positions;
        }
    }
}