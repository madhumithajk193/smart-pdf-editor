package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.PdfPropertiesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pdf-properties")
@CrossOrigin(origins = "*")
public class PdfPropertiesController {

    @Autowired
    private PdfPropertiesService pdfPropertiesService;


    // ============================================================
    // GET PDF PROPERTIES
    // ============================================================

    @GetMapping("/{pdfId}")
    public ResponseEntity<?> getPdfProperties(
            @PathVariable Long pdfId
    ) {

        try {

            Map<String, Object> properties =
                    pdfPropertiesService.getProperties(pdfId);

            return ResponseEntity.ok(properties);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    e.getMessage()
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "success",
                                    false,
                                    "message",
                                    "Failed to read PDF properties",
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}