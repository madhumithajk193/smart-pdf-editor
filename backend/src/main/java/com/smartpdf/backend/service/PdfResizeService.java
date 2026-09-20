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
public class PdfResizeService {

    @Autowired
    private PdfRepository pdfRepository;


    // ============================================================
    // RESIZE PDF
    // ============================================================

    public String resizePdf(
            Long pdfId,
            String pageSize,
            String orientation,
            String applyTo,
            Integer pageNumber,
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


        if (pageSize == null ||
                pageSize.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Page size is required"
            );
        }


        if (orientation == null ||
                orientation.trim().isEmpty()) {

            orientation = "PORTRAIT";
        }


        if (applyTo == null ||
                applyTo.trim().isEmpty()) {

            applyTo = "ALL";
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


        // --------------------------------------------------------
        // OUTPUT FILE
        // --------------------------------------------------------

        String finalOutputFileName =
                outputFileName;


        if (finalOutputFileName == null ||
                finalOutputFileName.trim().isEmpty()) {

            finalOutputFileName =
                    "resized-" +
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
        // GET PAGE SIZE
        // --------------------------------------------------------

        PDRectangle targetSize =
                getPageSize(
                        pageSize
                );


        // --------------------------------------------------------
        // ORIENTATION
        // --------------------------------------------------------

        if ("LANDSCAPE".equalsIgnoreCase(
                orientation
        )) {

            targetSize =
                    new PDRectangle(
                            targetSize.getHeight(),
                            targetSize.getWidth()
                    );
        }


        // --------------------------------------------------------
        // LOAD PDF
        // --------------------------------------------------------

        try (
                PDDocument document =
                        Loader.loadPDF(inputFile)
        ) {


            int totalPages =
                    document.getNumberOfPages();


            // ----------------------------------------------------
            // RESIZE ALL PAGES
            // ----------------------------------------------------

            if ("ALL".equalsIgnoreCase(
                    applyTo
            )) {

                for (
                        PDPage page :
                        document.getPages()
                ) {

                    resizePage(
                            page,
                            targetSize
                    );
                }

            }


            // ----------------------------------------------------
            // RESIZE CURRENT PAGE
            // ----------------------------------------------------

            else if (
                    "CURRENT".equalsIgnoreCase(
                            applyTo
                    )
            ) {


                if (
                        pageNumber == null ||
                                pageNumber < 1 ||
                                pageNumber > totalPages
                ) {

                    throw new IllegalArgumentException(
                            "Invalid page number: "
                                    + pageNumber
                    );
                }


                PDPage page =
                        document.getPage(
                                pageNumber - 1
                        );


                resizePage(
                        page,
                        targetSize
                );
            }


            else {

                throw new IllegalArgumentException(
                        "applyTo must be ALL or CURRENT"
                );
            }


            // ----------------------------------------------------
            // SAVE
            // ----------------------------------------------------

            document.save(
                    outputPath.toFile()
            );
        }


        // --------------------------------------------------------
        // VERIFY
        // --------------------------------------------------------

        if (!Files.exists(outputPath)) {

            throw new RuntimeException(
                    "Resized PDF was not created"
            );
        }


        if (Files.size(outputPath) == 0) {

            throw new RuntimeException(
                    "Resized PDF is empty"
            );
        }


        System.out.println(
                "================================="
        );

        System.out.println(
                "PDF RESIZED SUCCESSFULLY"
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
                "Page size: "
                        + pageSize
        );

        System.out.println(
                "Orientation: "
                        + orientation
        );

        System.out.println(
                "Apply to: "
                        + applyTo
        );

        System.out.println(
                "================================="
        );


        return outputPath
                .toAbsolutePath()
                .toString();
    }


    // ============================================================
    // RESIZE PAGE
    // ============================================================

    private void resizePage(
            PDPage page,
            PDRectangle targetSize
    ) {

        page.setMediaBox(
                targetSize
        );

        page.setCropBox(
                targetSize
        );

        page.setBleedBox(
                targetSize
        );

        page.setTrimBox(
                targetSize
        );

        page.setArtBox(
                targetSize
        );
    }


    // ============================================================
    // PAGE SIZE
    // ============================================================

    private PDRectangle getPageSize(
            String pageSize
    ) {

        if ("A4".equalsIgnoreCase(pageSize)) {

            return PDRectangle.A4;
        }


        if ("A3".equalsIgnoreCase(pageSize)) {

            return PDRectangle.A3;
        }


        if ("A5".equalsIgnoreCase(pageSize)) {

            return PDRectangle.A5;
        }


        if ("LETTER".equalsIgnoreCase(pageSize)) {

            return PDRectangle.LETTER;
        }


        if ("LEGAL".equalsIgnoreCase(pageSize)) {

            return PDRectangle.LEGAL;
        }


        throw new IllegalArgumentException(
                "Unsupported page size: "
                        + pageSize
                        + ". Supported sizes: "
                        + "A3, A4, A5, LETTER, LEGAL"
        );
    }
}