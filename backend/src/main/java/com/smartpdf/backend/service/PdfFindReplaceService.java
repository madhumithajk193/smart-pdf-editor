package com.smartpdf.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PdfFindReplaceService {

    // ============================================================
    // MAIN FIND AND REPLACE
    // ============================================================

    public String findAndReplace(
            String inputPath,
            String searchText,
            String replaceText,
            String outputFileName
    ) throws IOException {

        if (inputPath == null || inputPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Input PDF path cannot be empty."
            );
        }

        if (searchText == null || searchText.isBlank()) {
            throw new IllegalArgumentException(
                    "Search text cannot be empty."
            );
        }

        if (replaceText == null) {
            replaceText = "";
        }

        searchText = searchText.trim();
        replaceText = replaceText.trim();


        // --------------------------------------------------------
        // INPUT FILE
        // --------------------------------------------------------

        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            throw new IOException(
                    "Input PDF does not exist: " + inputPath
            );
        }


        // --------------------------------------------------------
        // UPLOAD DIRECTORY
        // --------------------------------------------------------

        Path uploadDirectory =
                Paths.get(
                                System.getProperty("user.dir"),
                                "uploads"
                        )
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(uploadDirectory);


        // --------------------------------------------------------
        // OUTPUT FILE
        // --------------------------------------------------------

        if (outputFileName == null ||
                outputFileName.isBlank()) {

            outputFileName =
                    "find-replace-result.pdf";
        }

        outputFileName =
                Paths.get(outputFileName)
                        .getFileName()
                        .toString();

        if (!outputFileName
                .toLowerCase(Locale.ROOT)
                .endsWith(".pdf")) {

            outputFileName += ".pdf";
        }

        Path outputPath =
                uploadDirectory
                        .resolve(outputFileName)
                        .normalize();


        // ========================================================
        // LOAD PDF
        // ========================================================

        try (
                PDDocument document =
                        Loader.loadPDF(inputFile)
        ) {

            boolean foundAnything = false;


            // ====================================================
            // PROCESS EACH PAGE
            // ====================================================

            for (
                    int pageIndex = 0;
                    pageIndex < document.getNumberOfPages();
                    pageIndex++
            ) {

                PDPage page =
                        document.getPage(pageIndex);


                System.out.println(
                        "================================"
                );

                System.out.println(
                        "Processing page "
                                + (pageIndex + 1)
                );


                // ------------------------------------------------
                // EXTRACT POSITIONS
                // ------------------------------------------------

                PositionExtractor extractor =
                        new PositionExtractor();

                extractor.setStartPage(
                        pageIndex + 1
                );

                extractor.setEndPage(
                        pageIndex + 1
                );

                /*
                 * IMPORTANT:
                 *
                 * Keep PDFBox's extraction order.
                 *
                 * Do NOT sort our own position list afterward.
                 */
                extractor.setSortByPosition(true);

                extractor.getText(document);


                List<TextPosition> positions =
                        extractor.getPositions();


                if (positions.isEmpty()) {
                    continue;
                }


                // ------------------------------------------------
                // FIND SEARCH TEXT
                // ------------------------------------------------

                List<TextMatch> matches =
                        findMatches(
                                positions,
                                searchText
                        );


                if (matches.isEmpty()) {
                    continue;
                }


                foundAnything = true;


                System.out.println(
                        "Found "
                                + matches.size()
                                + " occurrence(s)"
                );


                // ------------------------------------------------
                // DRAW REPLACEMENTS
                // ------------------------------------------------

                drawReplacements(
                        document,
                        page,
                        matches,
                        replaceText
                );
            }


            // ====================================================
            // NOT FOUND
            // ====================================================

            if (!foundAnything) {

                throw new IllegalArgumentException(
                        "Search text was not found in the PDF: "
                                + searchText
                );
            }


            // ====================================================
            // SAVE
            // ====================================================

            document.save(
                    outputPath.toFile()
            );
        }


        // ========================================================
        // VERIFY
        // ========================================================

        if (!Files.exists(outputPath)) {

            throw new IOException(
                    "Output PDF was not created."
            );
        }


        System.out.println(
                "================================"
        );

        System.out.println(
                "Find and Replace completed"
        );

        System.out.println(
                "Output: " + outputPath
        );

        System.out.println(
                "================================"
        );


        return outputPath
                .toAbsolutePath()
                .toString();
    }


    // ============================================================
    // FIND MATCHES
    // ============================================================

    private List<TextMatch> findMatches(
            List<TextPosition> positions,
            String searchText
    ) {

        List<TextMatch> matches =
                new ArrayList<>();


        if (positions == null ||
                positions.isEmpty()) {

            return matches;
        }


        // --------------------------------------------------------
        // TARGET
        // --------------------------------------------------------

        String target =
                normalizeSearch(searchText);


        if (target.isEmpty()) {
            return matches;
        }


        // --------------------------------------------------------
        // BUILD SEARCH STRING
        //
        // IMPORTANT:
        //
        // We preserve the order supplied by PDFTextStripper.
        // We do not globally sort the positions.
        // --------------------------------------------------------

        StringBuilder searchable =
                new StringBuilder();


        List<TextPosition> characterPositions =
                new ArrayList<>();


        for (
                TextPosition position :
                positions
        ) {

            String unicode =
                    position.getUnicode();


            if (unicode == null ||
                    unicode.isEmpty()) {

                continue;
            }


            /*
             * A TextPosition can contain more than one character.
             *
             * We associate every character with its
             * TextPosition.
             */

            for (
                    int i = 0;
                    i < unicode.length();
                    i++
            ) {

                char c =
                        unicode.charAt(i);


                // Ignore spaces when searching.
                if (Character.isWhitespace(c)) {
                    continue;
                }


                searchable.append(
                        Character.toLowerCase(c)
                );


                characterPositions.add(
                        position
                );
            }
        }


        String pdfText =
                searchable.toString();


        System.out.println(
                "Search target: "
                        + target
        );

        System.out.println(
                "PDF searchable text: "
                        + pdfText
        );


        // --------------------------------------------------------
        // FIND ALL OCCURRENCES
        // --------------------------------------------------------

        int searchFrom = 0;


        while (searchFrom < pdfText.length()) {

            int found =
                    pdfText.indexOf(
                            target,
                            searchFrom
                    );


            if (found == -1) {
                break;
            }


            int end =
                    found + target.length();


            if (end > characterPositions.size()) {
                break;
            }


            // ----------------------------------------------------
            // COLLECT POSITIONS
            // ----------------------------------------------------

            List<TextPosition> matched =
                    new ArrayList<>();


            for (
                    int i = found;
                    i < end;
                    i++
            ) {

                matched.add(
                        characterPositions.get(i)
                );
            }


            // ----------------------------------------------------
            // REMOVE DUPLICATES
            //
            // One TextPosition may represent multiple characters.
            // ----------------------------------------------------

            List<TextPosition> unique =
                    new ArrayList<>();


            for (
                    TextPosition position :
                    matched
            ) {

                if (!unique.contains(position)) {
                    unique.add(position);
                }
            }


            TextMatch match =
                    createMatch(unique);


            if (match != null) {

                matches.add(
                        match
                );


                System.out.println(
                        "Match found at X="
                                + match.firstX
                                + " Y="
                                + match.firstY
                );
            }


            searchFrom =
                    found +
                            Math.max(
                                    1,
                                    target.length()
                            );
        }


        return matches;
    }


    // ============================================================
    // CREATE MATCH
    // ============================================================

    private TextMatch createMatch(
            List<TextPosition> positions
    ) {

        if (positions == null ||
                positions.isEmpty()) {

            return null;
        }


        // --------------------------------------------------------
        // GROUP POSITIONS BY VISUAL LINE
        // --------------------------------------------------------

        List<TextLine> lines =
                new ArrayList<>();


        final float lineTolerance = 3.0f;


        for (
                TextPosition position :
                positions
        ) {

            TextLine selectedLine = null;


            for (
                    TextLine line :
                    lines
            ) {

                if (
                        Math.abs(
                                line.getAverageY()
                                        - position.getYDirAdj()
                        )
                                <= lineTolerance
                ) {

                    selectedLine =
                            line;

                    break;
                }
            }


            if (selectedLine == null) {

                selectedLine =
                        new TextLine();

                lines.add(
                        selectedLine
                );
            }


            selectedLine.positions.add(
                    position
            );
        }


        // --------------------------------------------------------
        // SORT LINES
        // --------------------------------------------------------

        lines.sort(
                Comparator.comparingDouble(
                        TextLine::getAverageY
                )
        );


        // --------------------------------------------------------
        // SORT POSITIONS INSIDE EACH LINE
        // --------------------------------------------------------

        for (
                TextLine line :
                lines
        ) {

            line.positions.sort(
                    Comparator.comparingDouble(
                            TextPosition::getXDirAdj
                    )
            );
        }


        TextPosition first =
                lines.get(0)
                        .positions
                        .get(0);


        return new TextMatch(
                lines,
                first.getXDirAdj(),
                first.getYDirAdj(),
                first.getHeightDir(),
                first.getFontSizeInPt()
        );
    }


    // ============================================================
    // DRAW REPLACEMENTS
    // ============================================================

    private void drawReplacements(
            PDDocument document,
            PDPage page,
            List<TextMatch> matches,
            String replaceText
    ) throws IOException {


        try (
                PDPageContentStream contentStream =
                        new PDPageContentStream(
                                document,
                                page,
                                AppendMode.APPEND,
                                true,
                                true
                        )
        ) {

            for (
                    TextMatch match :
                    matches
            ) {

                drawSingleReplacement(
                        contentStream,
                        page,
                        match,
                        replaceText
                );
            }
        }
    }


    // ============================================================
    // DRAW SINGLE REPLACEMENT
    // ============================================================

    private void drawSingleReplacement(
            PDPageContentStream contentStream,
            PDPage page,
            TextMatch match,
            String replaceText
    ) throws IOException {


        // ========================================================
        // 1. COVER ORIGINAL TEXT
        // ========================================================

        for (
                TextLine line :
                match.lines
        ) {

            if (line.positions.isEmpty()) {
                continue;
            }


            float minX =
                    Float.MAX_VALUE;

            float maxX =
                    -Float.MAX_VALUE;

            float minY =
                    Float.MAX_VALUE;

            float maxY =
                    -Float.MAX_VALUE;


            for (
                    TextPosition position :
                    line.positions
            ) {

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


            float pageHeight =
                    page.getMediaBox()
                            .getHeight();


            /*
             * YDirAdj starts from the TOP.
             *
             * PDF drawing coordinates start from the BOTTOM.
             *
             * Therefore:
             *
             * bottom =
             * pageHeight - maxY
             */

            float pdfY =
                    pageHeight - maxY;


            float width =
                    maxX - minX;


            float height =
                    maxY - minY;


            if (width <= 0) {
                continue;
            }


            if (height <= 0) {
                height = match.fontSize;
            }


            // Small padding around original text.
            float paddingX = 2.5f;
            float paddingY = 2.0f;


            contentStream.saveGraphicsState();


            // WHITE
            contentStream.setNonStrokingColor(
                    1f,
                    1f,
                    1f
            );


            contentStream.addRect(
                    minX - paddingX,
                    pdfY - paddingY,
                    width + paddingX * 2,
                    height + paddingY * 2
            );


            contentStream.fill();


            contentStream.restoreGraphicsState();
        }


        // ========================================================
        // 2. NOTHING TO WRITE
        // ========================================================

        if (
                replaceText == null ||
                        replaceText.isBlank()
        ) {

            return;
        }


        String replacement =
                cleanReplacement(
                        replaceText
                );


        if (replacement.isEmpty()) {
            return;
        }


        // ========================================================
        // 3. FONT
        // ========================================================

        PDType1Font font =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );


        float fontSize =
                Math.max(
                        6f,
                        match.fontSize
                );


        // ========================================================
        // 4. FIND AVAILABLE WIDTH
        // ========================================================

        TextLine firstLine =
                match.lines.get(0);


        float availableWidth =
                calculateLineWidth(
                        firstLine
                );


        if (availableWidth <= 0) {
            availableWidth = 100f;
        }


        availableWidth -= 4f;


        if (availableWidth < 20f) {
            availableWidth = 20f;
        }


        // ========================================================
        // 5. REDUCE FONT IF NEEDED
        // ========================================================

        float textWidth =
                font.getStringWidth(
                        replacement
                )
                        / 1000f
                        * fontSize;


        while (
                textWidth > availableWidth &&
                        fontSize > 6f
        ) {

            fontSize -= 0.5f;


            textWidth =
                    font.getStringWidth(
                            replacement
                    )
                            / 1000f
                            * fontSize;
        }


        // ========================================================
        // 6. REPLACEMENT POSITION
        // ========================================================

        TextPosition firstPosition =
                firstLine.positions.get(0);


        float x =
                firstPosition.getXDirAdj();


        /*
         * getYDirAdj() is TOP based.
         *
         * We convert that to the PDF bottom-based
         * coordinate system.
         *
         * The old code used:
         *
         * pageHeight - y - height
         *
         * which positioned the replacement too low
         * for this particular PDF.
         *
         * Here we calculate the baseline from the
         * TextPosition height.
         */

        float pageHeight =
                page.getMediaBox()
                        .getHeight();


        float topY =
                firstPosition.getYDirAdj();


        float textHeight =
                firstPosition.getHeightDir();


        float baseline =
                pageHeight
                        - topY
                        - textHeight
                        + (textHeight - fontSize) * 0.35f;


        // ========================================================
        // 7. DRAW REPLACEMENT
        // ========================================================

        contentStream.saveGraphicsState();


        contentStream.setNonStrokingColor(
                0f,
                0f,
                0f
        );


        contentStream.beginText();


        contentStream.setFont(
                font,
                fontSize
        );


        contentStream.newLineAtOffset(
                x,
                baseline
        );


        contentStream.showText(
                replacement
        );


        contentStream.endText();


        contentStream.restoreGraphicsState();
    }


    // ============================================================
    // CALCULATE LINE WIDTH
    // ============================================================

    private float calculateLineWidth(
            TextLine line
    ) {

        if (
                line == null ||
                        line.positions.isEmpty()
        ) {

            return 0f;
        }


        float minX =
                Float.MAX_VALUE;

        float maxX =
                -Float.MAX_VALUE;


        for (
                TextPosition position :
                line.positions
        ) {

            float x =
                    position.getXDirAdj();


            float width =
                    position.getWidthDirAdj();


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
        }


        return maxX - minX;
    }


    // ============================================================
    // NORMALIZE SEARCH
    // ============================================================

    private String normalizeSearch(
            String text
    ) {

        if (text == null) {
            return "";
        }


        StringBuilder result =
                new StringBuilder();


        for (
                int i = 0;
                i < text.length();
                i++
        ) {

            char c =
                    text.charAt(i);


            /*
             * Ignore:
             *
             * space
             * newline
             * tab
             *
             * Therefore:
             *
             * madhu mithajk
             *
             * matches:
             *
             * madhu
             * mithajk
             */

            if (Character.isWhitespace(c)) {
                continue;
            }


            result.append(
                    Character.toLowerCase(c)
            );
        }


        return result.toString();
    }


    // ============================================================
    // CLEAN REPLACEMENT
    // ============================================================

    private String cleanReplacement(
            String text
    ) {

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


    // ============================================================
    // POSITION EXTRACTOR
    // ============================================================

    private static class PositionExtractor
            extends PDFTextStripper {


        private final List<TextPosition>
                positions =
                new ArrayList<>();


        public PositionExtractor()
                throws IOException {

            super();

            setSortByPosition(true);
        }


        @Override
        protected void writeString(
                String text,
                List<TextPosition> textPositions
        ) throws IOException {


            /*
             * Keep the positions exactly in the order
             * provided by PDFTextStripper.
             */

            if (textPositions != null) {

                positions.addAll(
                        textPositions
                );
            }


            super.writeString(
                    text,
                    textPositions
            );
        }


        public List<TextPosition>
        getPositions() {

            return positions;
        }
    }


    // ============================================================
    // TEXT LINE
    // ============================================================

    private static class TextLine {

        private final List<TextPosition>
                positions =
                new ArrayList<>();


        private double getAverageY() {

            if (positions.isEmpty()) {
                return 0;
            }


            double total = 0;


            for (
                    TextPosition position :
                    positions
            ) {

                total +=
                        position.getYDirAdj();
            }


            return total /
                    positions.size();
        }
    }


    // ============================================================
    // TEXT MATCH
    // ============================================================

    private static class TextMatch {

        private final List<TextLine>
                lines;

        private final float firstX;

        private final float firstY;

        private final float firstHeight;

        private final float fontSize;


        public TextMatch(
                List<TextLine> lines,
                float firstX,
                float firstY,
                float firstHeight,
                float fontSize
        ) {

            this.lines =
                    lines;

            this.firstX =
                    firstX;

            this.firstY =
                    firstY;

            this.firstHeight =
                    firstHeight;

            this.fontSize =
                    fontSize;
        }
    }
}