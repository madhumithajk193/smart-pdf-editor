
package com.smartpdf.backend.repository;

import com.smartpdf.backend.entity.PdfFile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PdfRepository extends JpaRepository<PdfFile, Long> {

    // Get only files belonging to a guest
    List<PdfFile> findAllByGuestId(String guestId);

    // Find a file only if it belongs to the guest
    Optional<PdfFile> findByIdAndGuestId(Long id, String guestId);

    // Delete only a file belonging to the guest
    void deleteByIdAndGuestId(Long id, String guestId);
}
