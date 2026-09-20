package com.smartpdf.backend.repository;

import com.smartpdf.backend.entity.CustomFormField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomFormFieldRepository
        extends JpaRepository<CustomFormField, Long> {

    List<CustomFormField> findByPdfFileId(Long pdfId);

    void deleteByPdfFileId(Long pdfId);
}