package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.PdfSignatureVerificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/signature")
@CrossOrigin(origins = "*")
public class PdfSignatureVerificationController {

    @Autowired
    private PdfSignatureVerificationService
            pdfSignatureVerificationService;


    // ============================================================
    // VERIFY PDF DIGITAL SIGNATURE
    // ============================================================

    @GetMapping("/verify/{pdfId}")
    public ResponseEntity<?> verifySignature(
            @PathVariable Long pdfId
    ) {

        try {

            Map<String, Object> result =
                    pdfSignatureVerificationService
                            .verifySignature(pdfId);

            return ResponseEntity.ok(result);

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
                                    "Failed to verify PDF signature",
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}