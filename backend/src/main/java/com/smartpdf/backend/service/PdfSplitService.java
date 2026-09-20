package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class PdfSplitService {

    @Autowired
    private PdfRepository pdfRepository;

    private final Path uploadDir =
            Paths.get(System.getProperty("user.dir"))
                    .resolve("uploads")
                    .toAbsolutePath()
                    .normalize();

    public List<String> splitPdf(
            Long id,
            String pages,
            String outputPrefix) throws IOException {

        Files.createDirectories(uploadDir);

        // Find original PDF
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException(
                                "PDF not found in database. ID: " + id
                        )
                );

        // Find source PDF
        Path inputPath = null;

        // First try database path
        if (pdfFile.getFilePath() != null &&
                !pdfFile.getFilePath().isBlank()) {

            Path databasePath =
                    Paths.get(pdfFile.getFilePath())
                            .toAbsolutePath()
                            .normalize();

            if (Files.exists(databasePath)) {
                inputPath = databasePath;
            }
        }

        // If database path doesn't exist,
        // try uploads + filename
        if (inputPath == null &&
                pdfFile.getFileName() != null) {

            Path fallbackPath =
                    uploadDir.resolve(
                            pdfFile.getFileName()
                    ).normalize();

            if (Files.exists(fallbackPath)) {
                inputPath = fallbackPath;
            }
        }

        // Source PDF completely missing
        if (inputPath == null) {

            throw new IOException(
                    "Source PDF not found.\n" +
                            "Database filename: " +
                            pdfFile.getFileName() + "\n" +
                            "Database path: " +
                            pdfFile.getFilePath() + "\n" +
                            "Upload folder: " +
                            uploadDir
            );
        }

        if (outputPrefix == null ||
                outputPrefix.isBlank()) {

            outputPrefix = "split";
        }

        // Remove dangerous path characters
        outputPrefix =
                Paths.get(outputPrefix)
                        .getFileName()
                        .toString();

        List<Integer> pageNumbers =
                parsePages(pages);

        List<String> savedFiles =
                new ArrayList<>();

        System.out.println("=================================");
        System.out.println("PDF SPLIT");
        System.out.println("ID       : " + id);
        System.out.println("Input    : " + inputPath);
        System.out.println("Output   : " + uploadDir);
        System.out.println("Pages    : " + pages);
        System.out.println("Prefix   : " + outputPrefix);
        System.out.println("=================================");

        try (PDDocument sourceDoc =
                     Loader.loadPDF(inputPath.toFile())) {

            int totalPages =
                    sourceDoc.getNumberOfPages();

            for (int pageNumber : pageNumbers) {

                if (pageNumber < 1 ||
                        pageNumber > totalPages) {

                    throw new IOException(
                            "Invalid page number: " +
                                    pageNumber +
                                    ". Total pages: " +
                                    totalPages
                    );
                }

                try (PDDocument newDoc =
                             new PDDocument()) {

                    PDPage page =
                            sourceDoc.getPage(
                                    pageNumber - 1
                            );

                    newDoc.importPage(page);

                    // Example:
                    // split_page_1.pdf
                    // split_page_2.pdf
                    String outputFileName =
                            outputPrefix +
                                    "_page_" +
                                    pageNumber +
                                    ".pdf";

                    Path outputPath =
                            uploadDir.resolve(
                                    outputFileName
                            ).normalize();

                    // Create physical PDF
                    newDoc.save(
                            outputPath.toFile()
                    );

                    // Save correct information in database
                    PdfFile splitPdf =
                            new PdfFile();

                    splitPdf.setFileName(
                            outputFileName
                    );

                    splitPdf.setFilePath(
                            outputPath
                                    .toAbsolutePath()
                                    .toString()
                    );

                    splitPdf.setFileSize(
                            Files.size(outputPath)
                    );

                    pdfRepository.save(splitPdf);

                    savedFiles.add(
                            outputPath
                                    .toAbsolutePath()
                                    .toString()
                    );

                    System.out.println(
                            "Created: " +
                                    outputPath
                    );
                }
            }
        }

        System.out.println("PDF SPLIT SUCCESS");

        return savedFiles;
    }

    private List<Integer> parsePages(
            String pages) throws IOException {

        List<Integer> pageNumbers =
                new ArrayList<>();

        if (pages == null ||
                pages.isBlank()) {

            throw new IOException(
                    "Pages cannot be empty"
            );
        }

        String[] parts =
                pages.split(",");

        for (String part : parts) {

            try {

                int page =
                        Integer.parseInt(
                                part.trim()
                        );

                pageNumbers.add(page);

            } catch (NumberFormatException e) {

                throw new IOException(
                        "Invalid page number: " +
                                part
                );
            }
        }

        return pageNumbers;
    }
}