package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

@Service
public class PdfPropertiesService {

    @Autowired
    private PdfRepository pdfRepository;


    // ============================================================
    // GET PDF PROPERTIES
    // ============================================================

    public Map<String, Object> getProperties(
            Long pdfId
    ) throws Exception {

        // ========================================================
        // FIND PDF
        // ========================================================

        PdfFile pdfFile =
                pdfRepository.findById(pdfId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "PDF not found with ID: "
                                                + pdfId
                                )
                        );


        // ========================================================
        // INPUT FILE
        // ========================================================

        File inputFile =
                new File(
                        pdfFile.getFilePath()
                );


        if (!inputFile.exists()) {

            throw new RuntimeException(
                    "PDF file does not exist: "
                            + pdfFile.getFilePath()
            );
        }


        // ========================================================
        // RESULT
        // ========================================================

        Map<String, Object> result =
                new HashMap<>();


        result.put(
                "success",
                true
        );

        result.put(
                "pdfId",
                pdfId
        );

        result.put(
                "fileName",
                pdfFile.getFileName()
        );

        result.put(
                "fileSize",
                inputFile.length()
        );

        result.put(
                "filePath",
                pdfFile.getFilePath()
        );


        // ========================================================
        // LOAD PDF
        // ========================================================

        try (
                PDDocument document =
                        Loader.loadPDF(inputFile)
        ) {

            // ====================================================
            // PAGE COUNT
            // ====================================================

            result.put(
                    "pageCount",
                    document.getNumberOfPages()
            );


            // ====================================================
            // PDF VERSION
            // ====================================================

            result.put(
                    "pdfVersion",
                    document.getVersion()
            );


            // ====================================================
            // DOCUMENT INFORMATION
            // ====================================================

            PDDocumentInformation information =
                    document.getDocumentInformation();


            if (information != null) {

                // =================================================
                // TITLE
                // =================================================

                result.put(
                        "title",
                        emptyIfNull(
                                information.getTitle()
                        )
                );


                // =================================================
                // AUTHOR
                // =================================================

                result.put(
                        "author",
                        emptyIfNull(
                                information.getAuthor()
                        )
                );


                // =================================================
                // SUBJECT
                // =================================================

                result.put(
                        "subject",
                        emptyIfNull(
                                information.getSubject()
                        )
                );


                // =================================================
                // KEYWORDS
                // =================================================

                result.put(
                        "keywords",
                        emptyIfNull(
                                information.getKeywords()
                        )
                );


                // =================================================
                // CREATOR
                // =================================================

                result.put(
                        "creator",
                        emptyIfNull(
                                information.getCreator()
                        )
                );


                // =================================================
                // PRODUCER
                // =================================================

                result.put(
                        "producer",
                        emptyIfNull(
                                information.getProducer()
                        )
                );


                // =================================================
                // CREATION DATE
                // =================================================

                result.put(
                        "creationDate",
                        formatDate(
                                information.getCreationDate()
                        )
                );


                // =================================================
                // MODIFICATION DATE
                // =================================================

                result.put(
                        "modificationDate",
                        formatDate(
                                information.getModificationDate()
                        )
                );

            } else {

                result.put(
                        "title",
                        ""
                );

                result.put(
                        "author",
                        ""
                );

                result.put(
                        "subject",
                        ""
                );

                result.put(
                        "keywords",
                        ""
                );

                result.put(
                        "creator",
                        ""
                );

                result.put(
                        "producer",
                        ""
                );

                result.put(
                        "creationDate",
                        ""
                );

                result.put(
                        "modificationDate",
                        ""
                );
            }
        }


        // ========================================================
        // RETURN RESULT
        // ========================================================

        return result;
    }


    // ============================================================
    // NULL STRING HANDLER
    // ============================================================

    private String emptyIfNull(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value;
    }


    // ============================================================
    // DATE FORMATTER
    // ============================================================

    private String formatDate(
            Calendar calendar
    ) {

        if (calendar == null) {
            return "";
        }


        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd HH:mm:ss"
                );


        return format.format(
                calendar.getTime()
        );
    }
}