package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfCropService {

    @Autowired
    private PdfRepository pdfRepository;


    // ============================================================
    // CROP PDF
    // ============================================================

    public String cropPdf(
            Long pdfId,
            Integer pageNumber,
            Float x,
            Float y,
            Float width,
            Float height,
            String outputFileName
    ) throws Exception {

        // --------------------------------------------------------
        // VALIDATION
        // --------------------------------------------------------

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


        // --------------------------------------------------------
        // FIND PDF
        // --------------------------------------------------------

        PdfFile pdfFile =
                pdfRepository
                        .findById(pdfId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "PDF not found with ID: "
                                                + pdfId
                                )
                        );


        // --------------------------------------------------------
        // INPUT FILE
        // --------------------------------------------------------

        String storedPath =
                pdfFile.getFilePath();

        if (storedPath == null ||
                storedPath.trim().isEmpty()) {

            throw new RuntimeException(
                    "PDF file path is empty"
            );
        }

        File inputFile =
                new File(storedPath);

        if (!inputFile.exists()) {

            throw new RuntimeException(
                    "PDF file does not exist: "
                            + storedPath
            );
        }


        // --------------------------------------------------------
        // OUTPUT FILE NAME
        // --------------------------------------------------------

        String finalOutputFileName =
                outputFileName;

        if (finalOutputFileName == null ||
                finalOutputFileName.trim().isEmpty()) {

            finalOutputFileName =
                    "cropped-" +
                            inputFile.getName();
        }


        // Prevent directory traversal
        finalOutputFileName =
                Path.of(finalOutputFileName)
                        .getFileName()
                        .toString();


        if (!finalOutputFileName
                .toLowerCase()
                .endsWith(".pdf")) {

            finalOutputFileName += ".pdf";
        }


        // --------------------------------------------------------
        // OUTPUT DIRECTORY
        // --------------------------------------------------------

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


        // --------------------------------------------------------
        // LOAD PDF
        // --------------------------------------------------------

        try (
                PDDocument document =
                        Loader.loadPDF(inputFile)
        ) {

            // ----------------------------------------------------
            // CHECK PAGE
            // ----------------------------------------------------

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


            // ----------------------------------------------------
            // GET PAGE
            // ----------------------------------------------------

            PDPage page =
                    document.getPage(
                            pageNumber - 1
                    );


            // ----------------------------------------------------
            // GET ORIGINAL PAGE BOX
            // ----------------------------------------------------

            PDRectangle mediaBox =
                    page.getMediaBox();

            float pageWidth =
                    mediaBox.getWidth();

            float pageHeight =
                    mediaBox.getHeight();


            // ----------------------------------------------------
            // VALIDATE CROP
            // ----------------------------------------------------

            if (x < 0) {
                x = (float) 0;
            }

            if (y < 0) {
                y = (float) 0;
            }


            if (x + width > pageWidth) {

                width =
                        pageWidth - x;
            }


            if (y + height > pageHeight) {

                height =
                        pageHeight - y;
            }


            if (width <= 0 ||
                    height <= 0) {

                throw new IllegalArgumentException(
                        "Invalid crop area. "
                                + "Page size: "
                                + pageWidth
                                + " x "
                                + pageHeight
                );
            }


            // ----------------------------------------------------
            // PDF COORDINATE SYSTEM
            //
            // Frontend:
            //     y = top of page
            //
            // PDFBox:
            //     y = bottom of page
            //
            // Convert frontend Y to PDF Y.
            // ----------------------------------------------------

            float cropBottom =
                    pageHeight
                            - y
                            - height;


            float cropLeft =
                    x;


            float cropRight =
                    x + width;


            float cropTop =
                    cropBottom + height;


            // ----------------------------------------------------
            // CREATE NEW CROP RECTANGLE
            // ----------------------------------------------------

            PDRectangle cropRectangle =
                    new PDRectangle();

            cropRectangle.setLowerLeftX(
                    cropLeft
            );

            cropRectangle.setLowerLeftY(
                    cropBottom
            );

            cropRectangle.setUpperRightX(
                    cropRight
            );

            cropRectangle.setUpperRightY(
                    cropTop
            );


            // ----------------------------------------------------
            // SET BOTH MEDIA BOX AND CROP BOX
            //
            // This makes the selected area become the
            // actual visible page area.
            // Everything outside the selected rectangle
            // is no longer visible.
            // ----------------------------------------------------

            page.setMediaBox(
                    cropRectangle
            );

            page.setCropBox(
                    cropRectangle
            );


            // ----------------------------------------------------
            // RESET BLEED / TRIM / ART BOXES
            // ----------------------------------------------------

            page.setBleedBox(
                    cropRectangle
            );

            page.setTrimBox(
                    cropRectangle
            );

            page.setArtBox(
                    cropRectangle
            );


            // ----------------------------------------------------
            // SAVE
            // ----------------------------------------------------

            document.save(
                    outputPath.toFile()
            );
        }


        // --------------------------------------------------------
        // VERIFY OUTPUT
        // --------------------------------------------------------

        if (!Files.exists(outputPath)) {

            throw new RuntimeException(
                    "Cropped PDF was not created"
            );
        }


        if (Files.size(outputPath) == 0) {

            throw new RuntimeException(
                    "Cropped PDF is empty"
            );
        }


        System.out.println(
                "================================="
        );

        System.out.println(
                "PDF CROPPED SUCCESSFULLY"
        );

        System.out.println(
                "Input: "
                        + inputFile.getAbsolutePath()
        );

        System.out.println(
                "Output: "
                        + outputPath.toAbsolutePath()
        );

        System.out.println(
                "Crop X: " + x
        );

        System.out.println(
                "Crop Y: " + y
        );

        System.out.println(
                "Crop Width: " + width
        );

        System.out.println(
                "Crop Height: " + height
        );

        System.out.println(
                "================================="
        );


        return outputPath
                .toAbsolutePath()
                .toString();
    }
}