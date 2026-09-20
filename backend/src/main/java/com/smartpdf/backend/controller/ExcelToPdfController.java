 package com.smartpdf.backend.controller;

import com.smartpdf.backend.service.ExcelToPdfService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/pdfs")
@CrossOrigin(origins = "http://localhost:5173")
public class ExcelToPdfController {

    @Autowired
    private ExcelToPdfService excelToPdfService;


    // =====================================================
    // EXCEL TO PDF
    // =====================================================

    @PostMapping("/excel-to-pdf")
    public ResponseEntity<?> convertExcelToPdf(
            @RequestParam String inputFileName,
            @RequestParam(defaultValue = "converted.pdf")
            String outputFileName
    ) {

        try {

            String outputPath =
                    excelToPdfService.convertExcelToPdf(
                            inputFileName,
                            outputFileName
                    );

            return ResponseEntity.ok(outputPath);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Excel to PDF conversion failed: "
                                    + e.getMessage()
                    );
        }
    }


    // =====================================================
    // DOWNLOAD PDF
    // =====================================================
    @GetMapping("/download-excel-pdf/{fileName}")
    public ResponseEntity<Resource> downloadPdf(
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


            fileName =
                    Paths.get(fileName)
                            .getFileName()
                            .toString();


            Path filePath =
                    uploadDir
                            .resolve(fileName)
                            .normalize();


            System.out.println("=================================");
            System.out.println("DOWNLOAD PDF");
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
                    new FileSystemResource(filePath);


            return ResponseEntity
                    .ok()
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

