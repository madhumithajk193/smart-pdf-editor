
        package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.PdfToWordService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/pdfs")
@CrossOrigin(origins = "http://localhost:5173")
public class PdfToWordController {

    @Autowired
    private PdfToWordService pdfToWordService;


    // =====================================================
    // PDF TO WORD
    // =====================================================

    @PostMapping("/pdf-to-word/{id}")
    public ResponseEntity<?> convertPdfToWord(
            @PathVariable Long id,
            @RequestParam(defaultValue = "converted.docx")
            String outputFileName) {

        try {

            String outputPath =
                    pdfToWordService.convertPdfToWord(
                            id,
                            outputFileName
                    );

            return ResponseEntity.ok(outputPath);

        } catch (IOException e) {

            return ResponseEntity
                    .internalServerError()
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // DOWNLOAD WORD
    // =====================================================

    @GetMapping("/download-word/{fileName}")
    public ResponseEntity<?> downloadWord(
            @PathVariable String fileName) {

        try {

            Path filePath =
                    Paths.get(System.getProperty("user.dir"))
                            .resolve("uploads")
                            .resolve(fileName)
                            .normalize();


            // Check file exists
            if (!Files.exists(filePath)) {

                return ResponseEntity
                        .notFound()
                        .build();
            }


            Resource resource =
                    new FileSystemResource(filePath);


            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\""
                    )
                    .contentType(
                            MediaType.APPLICATION_OCTET_STREAM
                    )
                    .body(resource);


        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(e.getMessage());
        }
    }
}

