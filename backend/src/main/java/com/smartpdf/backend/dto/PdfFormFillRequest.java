package com.smartpdf.backend.dto;

import com.smartpdf.backend.entity.CustomFormField;

import java.util.List;

public class PdfFormFillRequest {

    private List<CustomFormField> fields;

    private String signatureImage;

    public List<CustomFormField> getFields() {
        return fields;
    }

    public void setFields(List<CustomFormField> fields) {
        this.fields = fields;
    }

    public String getSignatureImage() {
        return signatureImage;
    }

    public void setSignatureImage(String signatureImage) {
        this.signatureImage = signatureImage;
    }
}