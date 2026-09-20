package com.smartpdf.backend.repository;

import com.smartpdf.backend.entity.PdfFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PdfRepository extends JpaRepository<PdfFile, Long> {
}