package com.smartpdf.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "custom_form_fields")
public class CustomFormField {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PDF that this field belongs to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdf_id", nullable = false)
    private PdfFile pdfFile;

    // Name used when filling the form
    @Column(nullable = false)
    private String fieldName;

    // TEXT or CHECKBOX
    @Column(nullable = false)
    private String fieldType;

    // PDF page number (starts from 1)
    @Column(nullable = false)
    private Integer pageNumber;

    // Position and size
    @Column(nullable = false)
    private Float x;

    @Column(nullable = false)
    private Float y;

    @Column(nullable = false)
    private Float width;

    @Column(nullable = false)
    private Float height;

    // Default/current value
    @Column(length = 2000)
    private String fieldValue;

    public CustomFormField() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PdfFile getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(PdfFile pdfFile) {
        this.pdfFile = pdfFile;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
    }

    public Float getWidth() {
        return width;
    }

    public void setWidth(Float width) {
        this.width = width;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public String getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(String fieldValue) {
        this.fieldValue = fieldValue;
    }
}