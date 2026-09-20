package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfSignatureVerificationService {

    @Autowired
    private PdfRepository pdfRepository;


    // ============================================================
    // VERIFY PDF DIGITAL SIGNATURE
    // ============================================================

    public Map<String, Object> verifySignature(
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


        // ========================================================
        // LOAD PDF
        // ========================================================

        try (
                PDDocument document =
                        Loader.loadPDF(inputFile)
        ) {


            // ====================================================
            // GET SIGNATURES
            // ====================================================

            List<PDSignature> signatures =
                    document.getSignatureDictionaries();


            // ====================================================
            // NO SIGNATURE
            // ====================================================

            if (signatures == null
                    || signatures.isEmpty()) {

                result.put(
                        "signed",
                        false
                );

                result.put(
                        "signatureCount",
                        0
                );

                result.put(
                        "message",
                        "No digital signature found in this PDF."
                );

                result.put(
                        "signatures",
                        new ArrayList<>()
                );

                return result;
            }


            // ====================================================
            // SIGNATURE FOUND
            // ====================================================

            result.put(
                    "signed",
                    true
            );

            result.put(
                    "signatureCount",
                    signatures.size()
            );

            result.put(
                    "message",
                    "Digital signature found."
            );


            // ====================================================
            // SIGNATURE LIST
            // ====================================================

            List<Map<String, Object>>
                    signatureList =
                    new ArrayList<>();


            // ====================================================
            // PROCESS EACH SIGNATURE
            // ====================================================

            for (
                    PDSignature signature :
                    signatures
            ) {


                Map<String, Object>
                        signatureInfo =
                        new HashMap<>();


                // =================================================
                // SIGNER NAME
                // =================================================

                String name =
                        signature.getName();

                if (name == null
                        || name.trim().isEmpty()) {

                    name = "Not provided";
                }

                signatureInfo.put(
                        "name",
                        name
                );


                // =================================================
                // LOCATION
                // =================================================

                String location =
                        signature.getLocation();

                if (location == null
                        || location.trim().isEmpty()) {

                    location = "Not provided";
                }

                signatureInfo.put(
                        "location",
                        location
                );


                // =================================================
                // REASON
                // =================================================

                String reason =
                        signature.getReason();

                if (reason == null
                        || reason.trim().isEmpty()) {

                    reason = "Not provided";
                }

                signatureInfo.put(
                        "reason",
                        reason
                );


                // =================================================
                // CONTACT INFORMATION
                // =================================================

                String contactInfo =
                        signature.getContactInfo();

                if (contactInfo == null
                        || contactInfo.trim().isEmpty()) {

                    contactInfo = "Not provided";
                }

                signatureInfo.put(
                        "contactInfo",
                        contactInfo
                );


                // =================================================
                // SUBFILTER
                // =================================================

                String subFilter =
                        signature.getSubFilter();

                if (subFilter == null
                        || subFilter.trim().isEmpty()) {

                    subFilter = "Not provided";
                }

                signatureInfo.put(
                        "subFilter",
                        subFilter
                );


                // =================================================
                // SIGNATURE DATE
                // =================================================

                if (signature.getSignDate() != null) {

                    SimpleDateFormat dateFormat =
                            new SimpleDateFormat(
                                    "yyyy-MM-dd HH:mm:ss"
                            );

                    signatureInfo.put(
                            "signDate",
                            dateFormat.format(
                                    signature.getSignDate()
                            )
                    );

                } else {

                    signatureInfo.put(
                            "signDate",
                            "Not provided"
                    );
                }


                // =================================================
                // SIGNATURE STATUS
                // =================================================

                signatureInfo.put(
                        "status",
                        "Signature present"
                );


                // =================================================
                // CRYPTOGRAPHIC VERIFICATION
                // =================================================

                signatureInfo.put(
                        "cryptographicVerification",
                        "Not verified"
                );


                // =================================================
                // ADD SIGNATURE
                // =================================================

                signatureList.add(
                        signatureInfo
                );
            }


            // ====================================================
            // ADD SIGNATURE LIST
            // ====================================================

            result.put(
                    "signatures",
                    signatureList
            );


            // ====================================================
            // VERIFICATION NOTE
            // ====================================================

            result.put(
                    "verificationNote",
                    "A digital signature was detected. "
                            + "Cryptographic certificate validation "
                            + "has not yet been performed."
            );
        }


        return result;
    }
}