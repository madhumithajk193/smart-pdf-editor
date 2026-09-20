package com.smartpdf.backend.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfOcrService {

    /*
     * IMPORTANT:
     * This must point to the same uploads directory used by
     * your PDF upload endpoint.
     */
    private static final String UPLOAD_DIR =
            "C:\\Users\\acer\\Downloads\\backend\\uploads";

    public List<Map<String, Object>> performOcr(
            String pdfPath,
            String language
    ) throws IOException, TesseractException {

        System.out.println("=================================");
        System.out.println("PDF OCR STARTED");
        System.out.println("PDF Path : " + pdfPath);
        System.out.println("Language : " + language);
        System.out.println("=================================");

        /*
         * Step 1:
         * Check the path stored in the database.
         */
        File pdfFile = new File(pdfPath);

        /*
         * Step 2:
         * If the database contains only a filename or an old path,
         * try to find the actual file inside uploads/.
         */
        if (!pdfFile.exists() || !pdfFile.isFile()) {

            String fileName = pdfFile.getName();

            File uploadFile = new File(
                    UPLOAD_DIR,
                    fileName
            );

            System.out.println("Original path does not exist.");
            System.out.println("Trying uploads directory:");
            System.out.println(uploadFile.getAbsolutePath());

            if (uploadFile.exists() && uploadFile.isFile()) {
                pdfFile = uploadFile;
            } else {
                throw new IOException(
                        "PDF file not found.\n" +
                                "Database path: " + pdfPath + "\n" +
                                "Tried path: " + uploadFile.getAbsolutePath()
                );
            }
        }

        System.out.println("Actual PDF file:");
        System.out.println(pdfFile.getAbsolutePath());
        System.out.println("File exists: " + pdfFile.exists());
        System.out.println("File size: " + pdfFile.length());

        /*
         * Step 3:
         * Validate language.
         */
        if (language == null || language.trim().isEmpty()) {
            language = "eng";
        }

        /*
         * Step 4:
         * Configure Tesseract.
         *
         * IMPORTANT:
         * Tesseract must be installed on Windows.
         *
         * Example:
         * C:\Program Files\Tesseract-OCR\tessdata
         */
        Tesseract tesseract = new Tesseract();

        tesseract.setDatapath(
                "C:\\Program Files\\Tesseract-OCR\\tessdata"
        );

        tesseract.setLanguage(language);

        /*
         * Step 5:
         * Open PDF using PDFBox 3.x.
         *
         * PDFBox 3 uses Loader.loadPDF().
         */
        try (PDDocument document = Loader.loadPDF(pdfFile)) {

            int totalPages = document.getNumberOfPages();

            System.out.println("Total Pages: " + totalPages);

            PDFRenderer renderer = new PDFRenderer(document);

            List<Map<String, Object>> results = new ArrayList<>();

            /*
             * Step 6:
             * Render each PDF page to an image.
             */
            for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {

                System.out.println(
                        "OCR processing page " +
                                (pageIndex + 1) +
                                "/" +
                                totalPages
                );

                BufferedImage image =
                        renderer.renderImageWithDPI(
                                pageIndex,
                                300,
                                ImageType.RGB
                        );

                /*
                 * Step 7:
                 * Run Tesseract OCR.
                 */
                String text = tesseract.doOCR(image);

                /*
                 * Step 8:
                 * Store page result.
                 */
                Map<String, Object> pageResult =
                        new HashMap<>();

                pageResult.put(
                        "page",
                        pageIndex + 1
                );

                pageResult.put(
                        "text",
                        text == null ? "" : text.trim()
                );

                results.add(pageResult);

                System.out.println(
                        "Page " +
                                (pageIndex + 1) +
                                " OCR completed."
                );
            }

            System.out.println("=================================");
            System.out.println("PDF OCR COMPLETED");
            System.out.println("Pages processed: " + totalPages);
            System.out.println("=================================");

            return results;
        }

    }}