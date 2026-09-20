package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.WordToPdfService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/pdfs")
@CrossOrigin(origins = "http://localhost:5173")
public class WordToPdfController {

    @Autowired
    private WordToPdfService wordToPdfService;


    // =====================================================
    // WORD TO PDF
    // =====================================================

    @PostMapping("/word-to-pdf/{id}")
    public ResponseEntity<?> convertWordToPdf(
            @PathVariable Long id,
            @RequestParam(defaultValue = "converted.pdf")
            String outputFileName) {

        try {

            String outputPath =
                    wordToPdfService.convertWordToPdf(
                            id,
                            outputFileName
                    );

            return ResponseEntity.ok(outputPath);

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(e.getMessage());
        }
    }


    // =====================================================
    // DOWNLOAD CONVERTED PDF
    // =====================================================

    @GetMapping("/download-pdf/{fileName}")
    public ResponseEntity<Resource> downloadPdf(
            @PathVariable String fileName) {

        try {

            Path uploadDir =
                    Paths.get(System.getProperty("user.dir"))
                            .resolve("uploads")
                            .toAbsolutePath()
                            .normalize();

            Path filePath =
                    uploadDir
                            .resolve(fileName)
                            .normalize();

            System.out.println("=================================");
            System.out.println("DOWNLOAD PDF");
            System.out.println("File name : " + fileName);
            System.out.println("File path : " + filePath);
            System.out.println("Exists    : " + java.nio.file.Files.exists(filePath));
            System.out.println("=================================");

            if (!java.nio.file.Files.exists(filePath)) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            Resource resource =
                    new FileSystemResource(filePath);

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.APPLICATION_PDF
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" +
                                    fileName +
                                    "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}