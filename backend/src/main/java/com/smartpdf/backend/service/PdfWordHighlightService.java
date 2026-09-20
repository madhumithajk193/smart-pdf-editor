package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PdfWordHighlightService {

    @Autowired
    private PdfRepository pdfRepository;


    // ============================================================
    // MAIN METHOD
    // ============================================================

    public String highlightWord(
            Long pdfId,
            String word,
            String outputFileName
    ) throws IOException {

        if (pdfId == null) {
            throw new IOException("PDF ID is required.");
        }

        if (word == null || word.trim().isEmpty()) {
            throw new IOException(
                    "Please enter a word or text to highlight."
            );
        }


        // --------------------------------------------------------
        // FIND PDF
        // --------------------------------------------------------

        PdfFile pdfFile =
                pdfRepository.findById(pdfId)
                        .orElseThrow(() ->
                                new IOException(
                                        "PDF not found with ID: "
                                                + pdfId
                                )
                        );


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


        // --------------------------------------------------------
        // OUTPUT DIRECTORY
        // --------------------------------------------------------

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


        // --------------------------------------------------------
        // OUTPUT FILE
        // --------------------------------------------------------

        if (outputFileName == null ||
                outputFileName.trim().isEmpty()) {

            outputFileName =
                    "highlighted.pdf";
        }


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


        Path outputPath =
                uploadDirectory
                        .resolve(outputFileName)
                        .normalize();


        // --------------------------------------------------------
        // NORMALIZE SEARCH WORD
        // --------------------------------------------------------

        String searchWord =
                normalizeForSearch(word);


        if (searchWord.isEmpty()) {
            throw new IOException(
                    "Search word is empty."
            );
        }


        // --------------------------------------------------------
        // LOAD PDF
        // --------------------------------------------------------

        try (
                PDDocument document =
                        Loader.loadPDF(inputFile)
        ) {

            for (
                    int pageIndex = 0;
                    pageIndex < document.getNumberOfPages();
                    pageIndex++
            ) {

                PDPage page =
                        document.getPage(
                                pageIndex
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

                extractor.setSortByPosition(
                        true
                );


                extractor.getText(
                        document
                );


                List<TextPosition> positions =
                        extractor.getPositions();


                if (positions.isEmpty()) {
                    continue;
                }


                // ------------------------------------------------
                // FIND ACTUAL WORD
                // ------------------------------------------------

                List<TextMatch> matches =
                        findMatches(
                                positions,
                                searchWord
                        );


                // ------------------------------------------------
                // UNDERLINE MATCHES
                // ------------------------------------------------

                for (
                        TextMatch match :
                        matches
                ) {

                    addUnderline(
                            document,
                            page,
                            match
                    );
                }
            }


            // ----------------------------------------------------
            // SAVE
            // ----------------------------------------------------

            document.save(
                    outputPath.toFile()
            );
        }


        if (!Files.exists(outputPath)) {
            throw new IOException(
                    "Highlighted PDF was not created."
            );
        }


        return outputPath
                .toAbsolutePath()
                .toString();
    }


    // ============================================================
    // FIND MATCHES
    // ============================================================

    private List<TextMatch> findMatches(
            List<TextPosition> positions,
            String searchWord
    ) {

        List<TextMatch> matches =
                new ArrayList<>();


        /*
         * IMPORTANT:
         *
         * We create TWO things together:
         *
         * searchableText
         * characterPositions
         *
         * If a character is included in searchableText,
         * its exact TextPosition is stored at the same index.
         *
         * Spaces are ignored for SEARCHING, but the mapping
         * between characters and their PDF positions remains
         * correct.
         */


        StringBuilder searchableText =
                new StringBuilder();


        List<TextPosition> characterPositions =
                new ArrayList<>();


        // --------------------------------------------------------
        // BUILD SEARCHABLE TEXT
        // --------------------------------------------------------

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


            for (
                    int i = 0;
                    i < unicode.length();
                    i++
            ) {

                char c =
                        unicode.charAt(i);


                /*
                 * Ignore whitespace while searching.
                 *
                 * Example:
                 *
                 * PDFText
                 *
                 * PDF Text
                 *
                 * PDF   Text
                 *
                 * all become:
                 *
                 * pdftext
                 */

                if (Character.isWhitespace(c)) {
                    continue;
                }


                searchableText.append(
                        Character.toLowerCase(c)
                );


                characterPositions.add(
                        position
                );
            }
        }


        String pdfText =
                searchableText.toString();


        // --------------------------------------------------------
        // SEARCH
        // --------------------------------------------------------

        int fromIndex = 0;


        while (
                fromIndex <
                        pdfText.length()
        ) {

            int foundIndex =
                    pdfText.indexOf(
                            searchWord,
                            fromIndex
                    );


            if (foundIndex == -1) {
                break;
            }


            int endIndex =
                    foundIndex +
                            searchWord.length();


            // ----------------------------------------------------
            // GET EXACT POSITIONS
            // ----------------------------------------------------

            List<TextPosition> matchedPositions =
                    new ArrayList<>();


            for (
                    int i = foundIndex;
                    i < endIndex;
                    i++
            ) {

                if (
                        i <
                                characterPositions.size()
                ) {

                    matchedPositions.add(
                            characterPositions.get(i)
                    );
                }
            }


            // ----------------------------------------------------
            // CREATE MATCH
            // ----------------------------------------------------

            TextMatch match =
                    createTextMatch(
                            matchedPositions
                    );


            if (match != null) {
                matches.add(match);
            }


            // ----------------------------------------------------
            // SEARCH NEXT OCCURRENCE
            // ----------------------------------------------------

            fromIndex =
                    foundIndex +
                            Math.max(
                                    searchWord.length(),
                                    1
                            );
        }


        return matches;
    }


    // ============================================================
    // CREATE MATCH FROM REAL PDF POSITIONS
    // ============================================================

    private TextMatch createTextMatch(
            List<TextPosition> positions
    ) {

        if (positions == null ||
                positions.isEmpty()) {

            return null;
        }


        float minX =
                Float.MAX_VALUE;

        float minY =
                Float.MAX_VALUE;

        float maxX =
                -Float.MAX_VALUE;

        float maxY =
                -Float.MAX_VALUE;


        // --------------------------------------------------------
        // GET EXACT BOUNDING BOX
        // --------------------------------------------------------

        for (
                TextPosition position :
                positions
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


            minY =
                    Math.min(
                            minY,
                            y
                    );


            maxX =
                    Math.max(
                            maxX,
                            x + width
                    );


            maxY =
                    Math.max(
                            maxY,
                            y + height
                    );
        }


        float width =
                maxX - minX;


        float height =
                maxY - minY;


        if (width <= 0 ||
                height <= 0) {

            return null;
        }


        return new TextMatch(
                minX,
                minY,
                width,
                height
        );
    }


    // ============================================================
    // DRAW UNDERLINE
    // ============================================================

    private void addUnderline(
            PDDocument document,
            PDPage page,
            TextMatch match
    ) throws IOException {


        float pageHeight =
                page.getMediaBox()
                        .getHeight();


        /*
         * PDFBox TextPosition Y starts from the TOP.
         *
         * PDF drawing coordinates start from the BOTTOM.
         *
         * Convert the position.
         */

        float textBottom =
                pageHeight
                        - match.y
                        - match.height;


        /*
         * Small gap between text and underline.
         */

        float underlineY =
                textBottom + 1.0f;


        float startX =
                match.x;


        float endX =
                match.x +
                        match.width;


        try (
                PDPageContentStream contentStream =
                        new PDPageContentStream(
                                document,
                                page,
                                PDPageContentStream.AppendMode.APPEND,
                                true,
                                true
                        )
        ) {


            // ----------------------------------------------------
            // UNDERLINE COLOR
            // ----------------------------------------------------

            // BLACK
            contentStream.setStrokingColor(
                    0,
                    0,
                    0
            );


            // ----------------------------------------------------
            // LINE THICKNESS
            // ----------------------------------------------------

            contentStream.setLineWidth(
                    1.2f
            );


            // ----------------------------------------------------
            // DRAW UNDERLINE
            // ----------------------------------------------------

            contentStream.moveTo(
                    startX,
                    underlineY
            );


            contentStream.lineTo(
                    endX,
                    underlineY
            );


            contentStream.stroke();
        }
    }


    // ============================================================
    // SEARCH NORMALIZATION
    // ============================================================

    private String normalizeForSearch(
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
             * Ignore spaces in the user's search.
             *
             * This means:
             *
             * "Download Resume"
             *
             * becomes:
             *
             * "downloadresume"
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
    // TEXT POSITION EXTRACTOR
    // ============================================================

    private static class PositionExtractor
            extends PDFTextStripper {


        private final List<TextPosition>
                positions =
                new ArrayList<>();


        public PositionExtractor()
                throws IOException {

            super();

            setSortByPosition(
                    true
            );
        }


        @Override
        protected void writeString(
                String text,
                List<TextPosition> textPositions
        ) throws IOException {


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
    // TEXT MATCH
    // ============================================================

    private static class TextMatch {

        private final float x;

        private final float y;

        private final float width;

        private final float height;


        public TextMatch(
                float x,
                float y,
                float width,
                float height
        ) {

            this.x =
                    x;

            this.y =
                    y;

            this.width =
                    width;

            this.height =
                    height;
        }
    }
}