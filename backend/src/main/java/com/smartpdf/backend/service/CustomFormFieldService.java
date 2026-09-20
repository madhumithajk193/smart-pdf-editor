package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.CustomFormField;
import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.CustomFormFieldRepository;
import com.smartpdf.backend.repository.PdfRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomFormFieldService {

    @Autowired
    private CustomFormFieldRepository fieldRepository;

    @Autowired
    private PdfRepository pdfRepository;


    // =========================================================
    // CREATE FORM FIELD
    // =========================================================

    public CustomFormField createField(
            Long pdfId,
            String fieldName,
            String fieldType,
            Integer pageNumber,
            Float x,
            Float y,
            Float width,
            Float height
    ) {

        // Find PDF
        PdfFile pdfFile = pdfRepository.findById(pdfId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "PDF not found with ID: " + pdfId
                        )
                );

        // Validation
        if (fieldName == null || fieldName.isBlank()) {
            throw new IllegalArgumentException(
                    "Field name cannot be empty"
            );
        }

        if (fieldType == null || fieldType.isBlank()) {
            throw new IllegalArgumentException(
                    "Field type cannot be empty"
            );
        }

        fieldType = fieldType.toUpperCase();

        if (!fieldType.equals("TEXT") &&
                !fieldType.equals("CHECKBOX")) {

            throw new IllegalArgumentException(
                    "Field type must be TEXT or CHECKBOX"
            );
        }

        if (pageNumber == null || pageNumber < 1) {
            throw new IllegalArgumentException(
                    "Page number must be greater than 0"
            );
        }

        if (x == null || y == null ||
                width == null || height == null) {

            throw new IllegalArgumentException(
                    "Field position and size are required"
            );
        }

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Field width and height must be greater than 0"
            );
        }

        // Make sure requested page exists
        try {
            org.apache.pdfbox.Loader.loadPDF(
                    new java.io.File(pdfFile.getFilePath())
            ).close();
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Unable to read PDF: " + e.getMessage()
            );
        }

        // Create field
        CustomFormField field =
                new CustomFormField();

        field.setPdfFile(pdfFile);
        field.setFieldName(fieldName.trim());
        field.setFieldType(fieldType);
        field.setPageNumber(pageNumber);
        field.setX(x);
        field.setY(y);
        field.setWidth(width);
        field.setHeight(height);

        // Default value
        if (fieldType.equals("CHECKBOX")) {
            field.setFieldValue("false");
        } else {
            field.setFieldValue("");
        }

        return fieldRepository.save(field);
    }


    // =========================================================
    // GET ALL FIELDS FOR PDF
    // =========================================================

    public List<CustomFormField> getFields(Long pdfId) {

        // Check PDF exists
        if (!pdfRepository.existsById(pdfId)) {
            throw new IllegalArgumentException(
                    "PDF not found with ID: " + pdfId
            );
        }

        return fieldRepository.findByPdfFileId(pdfId);
    }


    // =========================================================
    // GET FIELD BY ID
    // =========================================================

    public CustomFormField getField(Long fieldId) {

        return fieldRepository.findById(fieldId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Form field not found with ID: "
                                        + fieldId
                        )
                );
    }


    // =========================================================
    // DELETE FIELD
    // =========================================================

    public void deleteField(Long fieldId) {

        if (!fieldRepository.existsById(fieldId)) {

            throw new IllegalArgumentException(
                    "Form field not found with ID: "
                            + fieldId
            );
        }

        fieldRepository.deleteById(fieldId);
    }


    // =========================================================
    // DELETE ALL FIELDS FOR PDF
    // =========================================================

    public void deleteFieldsForPdf(Long pdfId) {

        if (!pdfRepository.existsById(pdfId)) {

            throw new IllegalArgumentException(
                    "PDF not found with ID: " + pdfId
            );
        }

        fieldRepository.deleteByPdfFileId(pdfId);
    }
}