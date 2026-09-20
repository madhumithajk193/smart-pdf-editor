package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.PdfCropService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/pdfs")
@CrossOrigin(origins = "*")
public class PdfCropController {

    @Autowired
    private PdfCropService pdfCropService;


    // ============================================================
    // CROP PDF
    // ============================================================

    @PostMapping("/crop/{pdfId}")
    public ResponseEntity<?> cropPdf(

            @PathVariable Long pdfId,

            @RequestParam Integer pageNumber,

            @RequestParam(required = false, defaultValue = "0")
            Float x,

            @RequestParam(required = false, defaultValue = "0")
            Float y,

            @RequestParam Float width,

            @RequestParam Float height,

            @RequestParam(
                    value = "outputFileName",
                    required = false
            )
            String outputFileName

    ) {

        try {

            // ----------------------------------------------------
            // Call service
            // ----------------------------------------------------

            String savedPath =
                    pdfCropService.cropPdf(
                            pdfId,
                            pageNumber,
                            x,
                            y,
                            width,
                            height,
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
                            "PDF cropped successfully",

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
                                    "Error cropping PDF",

                                    "error",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Unknown error"
                            )
                    );
        }
    }
}