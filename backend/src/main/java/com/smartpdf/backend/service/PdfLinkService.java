package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;

import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDBorderStyleDictionary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class PdfLinkService {

    @Autowired
    private PdfRepository pdfRepository;


    // ============================================================
    // ADD PDF LINK
    // ============================================================

    public String addLink(
            Long pdfId,
            Integer pageNumber,
            Float x,
            Float y,
            Float width,
            Float height,
            String url,
            String outputFileName
    ) throws Exception {

        // ========================================================
        // VALIDATION
        // ========================================================

        if (pdfId == null) {
            throw new IllegalArgumentException(
                    "PDF ID is required"
            );
        }

        if (pageNumber == null || pageNumber < 1) {
            throw new IllegalArgumentException(
                    "Page number must be at least 1"
            );
        }

        if (x == null) {
            x = 0f;
        }

        if (y == null) {
            y = 0f;
        }

        if (width == null || width <= 0) {
            throw new IllegalArgumentException(
                    "Width must be greater than 0"
            );
        }

        if (height == null || height <= 0) {
            throw new IllegalArgumentException(
                    "Height must be greater than 0"
            );
        }

        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "URL is required"
            );
        }

        if (!url.startsWith("http://")
                && !url.startsWith("https://")) {

            throw new IllegalArgumentException(
                    "URL must start with http:// or https://"
            );
        }


        // ========================================================
        // FIND PDF
        // ========================================================

        PdfFile pdfFile =
                pdfRepository.findById(pdfId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "PDF not found with ID: "
                                                + pdfId
                                )
                        );


        // ========================================================
        // INPUT FILE
        // ========================================================

        File inputFile =
                new File(
                        pdfFile.getFilePath()
                );

        if (!inputFile.exists()) {

            throw new RuntimeException(
                    "PDF file does not exist: "
                            + pdfFile.getFilePath()
            );
        }


        // ========================================================
        // OUTPUT FILE NAME
        // ========================================================

        String finalOutputFileName =
                outputFileName;

        if (finalOutputFileName == null
                || finalOutputFileName.trim().isEmpty()) {

            finalOutputFileName =
                    "linked-" +
                            inputFile.getName();
        }

        finalOutputFileName =
                Path.of(
                                finalOutputFileName
                        )
                        .getFileName()
                        .toString();

        if (!finalOutputFileName
                .toLowerCase()
                .endsWith(".pdf")) {

            finalOutputFileName += ".pdf";
        }


        // ========================================================
        // OUTPUT DIRECTORY
        // ========================================================

        Path outputDirectory =
                inputFile.toPath()
                        .getParent();

        if (outputDirectory == null) {

            throw new RuntimeException(
                    "Unable to determine output directory"
            );
        }

        Files.createDirectories(
                outputDirectory
        );


        Path outputPath =
                outputDirectory.resolve(
                        finalOutputFileName
                );


        // ========================================================
        // LOAD PDF
        // ========================================================

        try (
                PDDocument document =
                        Loader.loadPDF(inputFile)
        ) {

            // ====================================================
            // CHECK PAGE
            // ====================================================

            int totalPages =
                    document.getNumberOfPages();

            if (pageNumber > totalPages) {

                throw new IllegalArgumentException(
                        "Page number "
                                + pageNumber
                                + " is invalid. PDF has "
                                + totalPages
                                + " pages."
                );
            }


            // ====================================================
            // GET PAGE
            // ====================================================

            PDPage page =
                    document.getPage(
                            pageNumber - 1
                    );


            // ====================================================
            // PAGE SIZE
            // ====================================================

            PDRectangle mediaBox =
                    page.getMediaBox();

            float pageHeight =
                    mediaBox.getHeight();


            // ====================================================
            // CONVERT COORDINATES
            // ====================================================

            float pdfX =
                    x;

            float pdfY =
                    pageHeight
                            - y
                            - height;


            // ====================================================
            // CREATE CLICKABLE LINK
            // ====================================================

            PDRectangle linkRectangle =
                    new PDRectangle(
                            pdfX,
                            pdfY,
                            width,
                            height
                    );

            PDAnnotationLink link =
                    new PDAnnotationLink();

            link.setRectangle(
                    linkRectangle
            );


            // ====================================================
            // URL ACTION
            // ====================================================

            PDActionURI action =
                    new PDActionURI();

            action.setURI(
                    url.trim()
            );

            link.setAction(
                    action
            );


            // ====================================================
            // REMOVE VISIBLE ANNOTATION BORDER
            // ====================================================

            PDBorderStyleDictionary border =
                    new PDBorderStyleDictionary();

            border.setWidth(0);

            link.setBorderStyle(
                    border
            );


            // ====================================================
            // CLICK HIGHLIGHT
            // ====================================================

            link.setHighlightMode(
                    PDAnnotationLink.HIGHLIGHT_MODE_INVERT
            );


            // ====================================================
            // ADD CLICKABLE LINK
            // ====================================================

            List<PDAnnotation> annotations =
                    page.getAnnotations();

            annotations.add(
                    link
            );

            page.setAnnotations(
                    annotations
            );


            // ====================================================
            // DRAW BLACK LINK TEXT
            // ====================================================

            drawBlackLinkText(
                    document,
                    page,
                    pdfX,
                    pdfY,
                    width,
                    height,
                    url.trim()
            );


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

            throw new RuntimeException(
                    "Linked PDF was not created"
            );
        }


        return outputPath
                .toAbsolutePath()
                .toString();
    }


    // ============================================================
    // DRAW BLACK LINK TEXT
    // ============================================================

    private void drawBlackLinkText(
            PDDocument document,
            PDPage page,
            float x,
            float y,
            float width,
            float height,
            String url
    ) throws Exception {


        // ========================================================
        // BLACK COLOR
        // ========================================================

        PDColor blackColor =
                new PDColor(
                        new float[]{
                                0.0f,
                                0.0f,
                                0.0f
                        },
                        PDDeviceRGB.INSTANCE
                );


        // ========================================================
        // FONT
        // ========================================================

        PDType1Font font =
                new PDType1Font(
                        Standard14Fonts.FontName.HELVETICA
                );


        float fontSize = 10f;


        // ========================================================
        // CALCULATE TEXT WIDTH
        // ========================================================

        float textWidth =
                font.getStringWidth(url)
                        / 1000f
                        * fontSize;


        // ========================================================
        // REDUCE FONT FOR LONG URL
        // ========================================================

        while (
                textWidth > width - 10
                        && fontSize > 5
        ) {

            fontSize -= 0.5f;

            textWidth =
                    font.getStringWidth(url)
                            / 1000f
                            * fontSize;
        }


        // ========================================================
        // DRAW TEXT
        // ========================================================

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


            // ====================================================
            // TEXT POSITION
            // ====================================================

            float textX =
                    x + 5;

            float textY =
                    y
                            + (height - fontSize)
                            / 2;


            // ====================================================
            // BLACK TEXT
            // ====================================================

            contentStream.beginText();

            contentStream.setNonStrokingColor(
                    blackColor
            );

            contentStream.setFont(
                    font,
                    fontSize
            );

            contentStream.newLineAtOffset(
                    textX,
                    textY
            );

            contentStream.showText(
                    url
            );

            contentStream.endText();


            // ====================================================
            // BLACK UNDERLINE
            // ====================================================

            float underlineY =
                    textY - 2;

            contentStream.setStrokingColor(
                    blackColor
            );

            contentStream.setLineWidth(
                    0.8f
            );

            contentStream.moveTo(
                    textX,
                    underlineY
            );

            contentStream.lineTo(
                    textX + textWidth,
                    underlineY
            );

            contentStream.stroke();
        }
    }
}