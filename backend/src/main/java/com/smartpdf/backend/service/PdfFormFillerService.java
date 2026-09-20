package com.smartpdf.backend.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfFormFillerService {

    // =========================================================
    // GET FORM FIELDS
    // =========================================================

    public List<Map<String, Object>> getFormFields(
            String inputPath
    ) throws IOException {

        if (inputPath == null || inputPath.isBlank()) {
            throw new IllegalArgumentException(
                    "Input PDF path cannot be empty"
            );
        }

        File inputFile = new File(inputPath);

        if (!inputFile.exists()) {
            throw new IOException(
                    "Input PDF does not exist: " + inputPath
            );
        }

        List<Map<String, Object>> fields =
                new ArrayList<>();

        try (PDDocument document =
                     Loader.loadPDF(inputFile)) {

            PDAcroForm acroForm =
                    document.getDocumentCatalog().getAcroForm();

            if (acroForm == null) {
                return fields;
            }

            for (PDField field :
                    acroForm.getFieldTree()) {

                Map<String, Object> fieldInfo =
                        new LinkedHashMap<>();

                fieldInfo.put(
                        "name",
                        field.getFullyQualifiedName()
                );

                fieldInfo.put(
                        "value",
                        field.getValueAsString()
                );

                fieldInfo.put(
                        "type",
                        field.getClass()
                                .getSimpleName()
                );

                fields.add(fieldInfo);
            }
        }

        return fields;
    }


    // =========================================================
    // FILL FORM
    // =========================================================

    public String fillForm(
            String inputPath,
            Map<String, String> fieldValues,
            String outputFileName
    ) throws IOException {

        if (inputPath == null ||
                inputPath.isBlank()) {

            throw new IllegalArgumentException(
                    "Input PDF path cannot be empty"
            );
        }

        File inputFile =
                new File(inputPath);

        if (!inputFile.exists()) {

            throw new IOException(
                    "Input PDF does not exist: "
                            + inputPath
            );
        }

        if (fieldValues == null ||
                fieldValues.isEmpty()) {

            throw new IllegalArgumentException(
                    "No form field values were provided"
            );
        }

        // -----------------------------------------------------
        // OUTPUT DIRECTORY
        // -----------------------------------------------------

        Path uploadDirectory =
                Paths.get(
                                System.getProperty("user.dir")
                        )
                        .resolve("uploads")
                        .toAbsolutePath()
                        .normalize();

        Files.createDirectories(
                uploadDirectory
        );

        // -----------------------------------------------------
        // OUTPUT FILE NAME
        // -----------------------------------------------------

        if (outputFileName == null ||
                outputFileName.isBlank()) {

            outputFileName =
                    "filled-form.pdf";
        }

        outputFileName =
                Paths.get(outputFileName)
                        .getFileName()
                        .toString();

        if (!outputFileName
                .toLowerCase()
                .endsWith(".pdf")) {

            outputFileName += ".pdf";
        }

        Path outputPath =
                uploadDirectory
                        .resolve(outputFileName)
                        .normalize();

        // -----------------------------------------------------
        // LOAD PDF
        // -----------------------------------------------------

        try (PDDocument document =
                     Loader.loadPDF(inputFile)) {

            PDAcroForm acroForm =
                    document
                            .getDocumentCatalog()
                            .getAcroForm();

            if (acroForm == null) {

                throw new IllegalArgumentException(
                        "This PDF does not contain an editable form."
                );
            }

            boolean foundField = false;

            // -------------------------------------------------
            // FILL FIELDS
            // -------------------------------------------------

            for (Map.Entry<String, String> entry :
                    fieldValues.entrySet()) {

                String fieldName =
                        entry.getKey();

                String value =
                        entry.getValue();

                if (fieldName == null ||
                        fieldName.isBlank()) {

                    continue;
                }

                PDField field =
                        acroForm.getField(
                                fieldName
                        );

                if (field == null) {

                    System.out.println(
                            "Form field not found: "
                                    + fieldName
                    );

                    continue;
                }

                foundField = true;

                if (value == null) {
                    value = "";
                }

                System.out.println(
                        "Filling field: "
                                + fieldName
                                + " = "
                                + value
                );

                field.setValue(value);
            }

            if (!foundField) {

                throw new IllegalArgumentException(
                        "None of the supplied form fields "
                                + "were found in the PDF."
                );
            }

            // -------------------------------------------------
            // MAKE FORM APPEARANCE VISIBLE
            // -------------------------------------------------

            acroForm.refreshAppearances();

            // -------------------------------------------------
            // SAVE
            // -------------------------------------------------

            document.save(
                    outputPath.toFile()
            );
        }

        System.out.println(
                "Filled PDF created: "
                        + outputPath
        );

        return outputPath
                .toAbsolutePath()
                .toString();
    }
}