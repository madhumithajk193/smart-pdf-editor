package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.PdfResizeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/pdfs")
@CrossOrigin(origins = "*")
public class PdfResizeController {

    @Autowired
    private PdfResizeService pdfResizeService;


    // ============================================================
    // RESIZE PDF
    // ============================================================

    @PostMapping("/resize/{pdfId}")
    public ResponseEntity<?> resizePdf(

            @PathVariable Long pdfId,

            @RequestParam String pageSize,

            @RequestParam(
                    required = false,
                    defaultValue = "PORTRAIT"
            )
            String orientation,

            @RequestParam(
                    required = false,
                    defaultValue = "ALL"
            )
            String applyTo,

            @RequestParam(
                    required = false
            )
            Integer pageNumber,

            @RequestParam(
                    required = false
            )
            String outputFileName

    ) {

        try {

            // ----------------------------------------------------
            // CALL SERVICE
            // ----------------------------------------------------

            String savedPath =
                    pdfResizeService.resizePdf(

                            pdfId,

                            pageSize,

                            orientation,

                            applyTo,

                            pageNumber,

                            outputFileName
                    );


            // ----------------------------------------------------
            // GET FILE NAME
            // ----------------------------------------------------

            String fileName =
                    Paths.get(savedPath)
                            .getFileName()
                            .toString();


            // ----------------------------------------------------
            // RESPONSE
            // ----------------------------------------------------

            return ResponseEntity.ok(

                    Map.of(

                            "message",
                            "PDF resized successfully",

                            "fileName",
                            fileName,

                            "downloadUrl",
                            "/api/pdfs/download-file/"
                                    + fileName
                    )
            );


        } catch (Exception e) {

            e.printStackTrace();


            return ResponseEntity
                    .status(
                            HttpStatus.BAD_REQUEST
                    )
                    .body(

                            Map.of(

                                    "message",
                                    "Error resizing PDF",

                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}