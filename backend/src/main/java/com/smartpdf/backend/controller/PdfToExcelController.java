package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.PdfToExcelService;

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
public class PdfToExcelController {

    @Autowired
    private PdfToExcelService pdfToExcelService;


    // =====================================================
    // PDF TO EXCEL
    // =====================================================

    @PostMapping("/pdf-to-excel/{id}")
    public ResponseEntity<?> convertPdfToExcel(
            @PathVariable Long id,
            @RequestParam(
                    defaultValue = "converted.xlsx"
            )
            String outputFileName
    ) {

        try {

            String outputPath =
                    pdfToExcelService.convertPdfToExcel(
                            id,
                            outputFileName
                    );

            return ResponseEntity.ok(
                    outputPath
            );

        } catch (IOException e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "PDF to Excel conversion failed: "
                                    + e.getMessage()
                    );
        }
    }


    // =====================================================
    // DOWNLOAD EXCEL
    // =====================================================

    @GetMapping("/download-excel/{fileName}")
    public ResponseEntity<Resource> downloadExcel(
            @PathVariable String fileName
    ) {

        try {

            Path uploadDir =
                    Paths.get(
                                    System.getProperty("user.dir")
                            )
                            .resolve("uploads")
                            .toAbsolutePath()
                            .normalize();

            Path filePath =
                    uploadDir
                            .resolve(fileName)
                            .normalize();


            System.out.println("=================================");
            System.out.println("DOWNLOAD EXCEL");
            System.out.println("File name : " + fileName);
            System.out.println("File path : " + filePath);
            System.out.println(
                    "Exists    : " +
                            Files.exists(filePath)
            );
            System.out.println("=================================");


            if (!Files.exists(filePath)) {

                return ResponseEntity
                        .notFound()
                        .build();
            }


            Resource resource =
                    new FileSystemResource(
                            filePath
                    );


            return ResponseEntity
                    .ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                            )
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