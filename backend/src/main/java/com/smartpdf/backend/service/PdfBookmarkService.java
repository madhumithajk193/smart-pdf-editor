package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class PdfBookmarkService {

    @Autowired
    private PdfRepository pdfRepository;


    // ============================================================
    // ADD BOOKMARK
    // ============================================================

    public String addBookmark(
            Long pdfId,
            String bookmarkTitle,
            Integer pageNumber,
            String outputFileName
    ) throws Exception {

        // --------------------------------------------------------
        // Validate input
        // --------------------------------------------------------

        if (pdfId == null) {
            throw new IllegalArgumentException(
                    "PDF ID is required"
            );
        }

        if (bookmarkTitle == null ||
                bookmarkTitle.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Bookmark title is required"
            );
        }

        if (pageNumber == null ||
                pageNumber < 1) {

            throw new IllegalArgumentException(
                    "Page number must be at least 1"
            );
        }


        // --------------------------------------------------------
        // Find PDF
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
        // Get PDF path
        // --------------------------------------------------------

        String inputPath =
                pdfFile.getFilePath();


        if (inputPath == null ||
                inputPath.trim().isEmpty()) {

            throw new RuntimeException(
                    "PDF file path is empty"
            );
        }


        File inputFile =
                new File(inputPath);


        if (!inputFile.exists()) {

            throw new RuntimeException(
                    "PDF file does not exist: "
                            + inputPath
            );
        }


        // --------------------------------------------------------
        // Load PDF
        // --------------------------------------------------------

        try (
                PDDocument document =
                        Loader.loadPDF(inputFile)
        ) {


            // ----------------------------------------------------
            // Check page number
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
            // Get target page
            // ----------------------------------------------------

            PDPage targetPage =
                    document.getPage(
                            pageNumber - 1
                    );


            // ----------------------------------------------------
            // Get existing bookmark tree
            // ----------------------------------------------------

            PDDocumentOutline outline =
                    document
                            .getDocumentCatalog()
                            .getDocumentOutline();


            // ----------------------------------------------------
            // Create bookmark tree if needed
            // ----------------------------------------------------

            if (outline == null) {

                outline =
                        new PDDocumentOutline();

                document
                        .getDocumentCatalog()
                        .setDocumentOutline(
                                outline
                        );
            }


            // ----------------------------------------------------
            // Create bookmark
            // ----------------------------------------------------

            PDOutlineItem bookmark =
                    new PDOutlineItem();


            bookmark.setTitle(
                    bookmarkTitle.trim()
            );


            // ----------------------------------------------------
            // Link bookmark to page
            // ----------------------------------------------------

            bookmark.setDestination(
                    targetPage
            );


            // ----------------------------------------------------
            // Add bookmark
            // ----------------------------------------------------

            outline.addLast(
                    bookmark
            );


            // ----------------------------------------------------
            // Open bookmark tree
            // ----------------------------------------------------

            outline.openNode();


            // ----------------------------------------------------
            // Output filename
            // ----------------------------------------------------

            String finalOutputFileName =
                    outputFileName;


            if (finalOutputFileName == null ||
                    finalOutputFileName.trim().isEmpty()) {

                finalOutputFileName =
                        "bookmarked-" +
                                inputFile.getName();
            }


            finalOutputFileName =
                    finalOutputFileName.trim();


            if (!finalOutputFileName
                    .toLowerCase()
                    .endsWith(".pdf")) {

                finalOutputFileName += ".pdf";
            }


            // ----------------------------------------------------
            // Output path
            // ----------------------------------------------------

            Path parentDirectory =
                    inputFile.toPath()
                            .getParent();


            if (parentDirectory == null) {

                throw new RuntimeException(
                        "Unable to determine PDF directory"
                );
            }


            Path outputPath =
                    parentDirectory.resolve(
                            finalOutputFileName
                    );


            // ----------------------------------------------------
            // Save
            // ----------------------------------------------------

            document.save(
                    outputPath.toFile()
            );


            // ----------------------------------------------------
            // Verify
            // ----------------------------------------------------

            if (!Files.exists(outputPath)) {

                throw new RuntimeException(
                        "Bookmark PDF was not created"
                );
            }


            return outputPath
                    .toAbsolutePath()
                    .toString();
        }
    }
}