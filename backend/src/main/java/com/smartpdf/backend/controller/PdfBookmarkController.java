package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.PdfBookmarkService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/pdfs")
@CrossOrigin(origins = "*")
public class PdfBookmarkController {

    @Autowired
    private PdfBookmarkService pdfBookmarkService;


    // ============================================================
    // ADD BOOKMARK
    // ============================================================

    @PostMapping("/bookmark/{pdfId}")
    public ResponseEntity<?> addBookmark(
            @PathVariable Long pdfId,

            @RequestParam String bookmarkTitle,

            @RequestParam Integer pageNumber,

            @RequestParam(
                    value = "outputFileName",
                    required = false
            )
            String outputFileName
    ) {

        try {

            // ----------------------------------------------------
            // Call bookmark service
            // ----------------------------------------------------

            String savedPath =
                    pdfBookmarkService.addBookmark(
                            pdfId,
                            bookmarkTitle,
                            pageNumber,
                            outputFileName
                    );


            // ----------------------------------------------------
            // Get generated filename
            // ----------------------------------------------------

            String fileName =
                    Paths.get(savedPath)
                            .getFileName()
                            .toString();


            // ----------------------------------------------------
            // Success response
            // ----------------------------------------------------

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "PDF bookmark added successfully",

                            "fileName",
                            fileName,

                            "downloadUrl",
                            "/api/pdfs/download-file/"
                                    + fileName
                    )
            );


        } catch (Exception e) {

            e.printStackTrace();


            // ----------------------------------------------------
            // Error response
            // ----------------------------------------------------

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            Map.of(
                                    "message",
                                    "Error adding PDF bookmark",

                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Unknown error"
                            )
                    );
        }
    }
}