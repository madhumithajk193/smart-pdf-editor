package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.PdfLinkService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pdfs")
@CrossOrigin(origins = "*")
public class PdfLinkController {

    @Autowired
    private PdfLinkService pdfLinkService;


    // ============================================================
    // ADD LINK TO PDF
    // ============================================================

    @PostMapping("/add-link")
    public ResponseEntity<?> addLink(
            @RequestParam Long pdfId,
            @RequestParam Integer pageNumber,
            @RequestParam Float x,
            @RequestParam Float y,
            @RequestParam Float width,
            @RequestParam Float height,
            @RequestParam String url,
            @RequestParam(required = false) String outputFileName
    ) {

        try {

            String outputPath =
                    pdfLinkService.addLink(
                            pdfId,
                            pageNumber,
                            x,
                            y,
                            width,
                            height,
                            url,
                            outputFileName
                    );


            // ----------------------------------------------------
            // GET OUTPUT FILE NAME
            // ----------------------------------------------------

            String fileName =
                    java.nio.file.Paths
                            .get(outputPath)
                            .getFileName()
                            .toString();


            // ----------------------------------------------------
            // RESPONSE
            // ----------------------------------------------------

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    true
            );

            response.put(
                    "message",
                    "PDF link added successfully"
            );

            response.put(
                    "fileName",
                    fileName
            );

            response.put(
                    "filePath",
                    outputPath
            );

            response.put(
                    "downloadUrl",
                    "/api/pdfs/download-file/"
                            + fileName
            );


            return ResponseEntity.ok(
                    response
            );


        } catch (IllegalArgumentException e) {

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "error",
                    e.getMessage()
            );


            return ResponseEntity
                    .badRequest()
                    .body(response);


        } catch (Exception e) {

            e.printStackTrace();

            Map<String, Object> response =
                    new HashMap<>();

            response.put(
                    "success",
                    false
            );

            response.put(
                    "error",
                    e.getMessage() != null
                            ? e.getMessage()
                            : "Failed to add PDF link"
            );


            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(response);
        }
    }
}