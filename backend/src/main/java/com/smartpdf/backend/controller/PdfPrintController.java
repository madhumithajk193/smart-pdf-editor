package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.PdfPrintService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping("/api/pdf-print")
@CrossOrigin(origins = "*")
public class PdfPrintController {

    @Autowired
    private PdfPrintService pdfPrintService;


    // ============================================================
    // GET PDF FOR PRINTING
    // ============================================================

    @GetMapping("/{pdfId}")
    public ResponseEntity<Resource> getPdfForPrinting(
            @PathVariable Long pdfId
    ) {

        try {

            File file =
                    pdfPrintService.getPdfForPrinting(
                            pdfId
                    );


            Resource resource =
                    new FileSystemResource(file);


            return ResponseEntity
                    .ok()
                    .contentType(
                            MediaType.APPLICATION_PDF
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" +
                                    file.getName() +
                                    "\""
                    )
                    .body(resource);


        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();


        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .build();
        }
    }
}