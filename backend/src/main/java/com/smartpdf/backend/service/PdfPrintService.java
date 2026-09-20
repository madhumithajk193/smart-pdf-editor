package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class PdfPrintService {

    @Autowired
    private PdfRepository pdfRepository;


    // ============================================================
    // GET PDF FILE FOR PRINTING
    // ============================================================

    public File getPdfForPrinting(Long pdfId) {

        // --------------------------------------------------------
        // FIND PDF
        // --------------------------------------------------------

        PdfFile pdfFile =
                pdfRepository.findById(pdfId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "PDF not found with ID: "
                                                + pdfId
                                )
                        );


        // --------------------------------------------------------
        // GET FILE
        // --------------------------------------------------------

        File file =
                new File(
                        pdfFile.getFilePath()
                );


        // --------------------------------------------------------
        // CHECK FILE
        // --------------------------------------------------------

        if (!file.exists()) {

            throw new RuntimeException(
                    "PDF file does not exist: "
                            + pdfFile.getFilePath()
            );
        }


        return file;
    }
}