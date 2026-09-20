package com.smartpdf.backend.service;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class PdfToExcelService {

    private final PdfRepository pdfRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDirectory;

    public PdfToExcelService(PdfRepository pdfRepository) {
        this.pdfRepository = pdfRepository;
    }

    public String convertPdfToExcel(
            Long id,
            String outputFileName
    ) throws IOException {

        // 1. Find PDF record from database
        PdfFile pdfFile = pdfRepository.findById(id)
                .orElseThrow(() ->
                        new IOException("PDF not found in database. ID: " + id)
                );

        // 2. Get actual PDF path
        String storedPath = pdfFile.getFilePath();

        if (storedPath == null || storedPath.isBlank()) {
            throw new IOException(
                    "PDF file path is empty for ID: " + id
            );
        }

        Path pdfPath = Paths.get(storedPath);

        // 3. Check that physical PDF exists
        if (!Files.exists(pdfPath)) {
            throw new IOException(
                    "PDF file does not exist: " + pdfPath
            );
        }

        if (!Files.isRegularFile(pdfPath)) {
            throw new IOException(
                    "PDF path is not a regular file: " + pdfPath
            );
        }

        // 4. Make sure output filename is .xlsx
        if (outputFileName == null ||
                outputFileName.isBlank()) {

            outputFileName = "converted.xlsx";
        }

        outputFileName = Paths.get(outputFileName)
                .getFileName()
                .toString();

        if (!outputFileName.toLowerCase().endsWith(".xlsx")) {
            outputFileName += ".xlsx";
        }

        // 5. Create uploads directory
        Path uploadPath = Paths.get(uploadDirectory);

        Files.createDirectories(uploadPath);

        /*
         * Use a unique filename so an existing Excel file
         * is never overwritten.
         */
        String uniqueFileName =
                UUID.randomUUID() + "_" + outputFileName;

        Path excelPath =
                uploadPath.resolve(uniqueFileName)
                        .normalize();

        // 6. Extract text from PDF
        String text;

        try (PDDocument document =
                     Loader.loadPDF(pdfPath.toFile())) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            text = stripper.getText(document);
        }

        // 7. Create Excel workbook
        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet("PDF Data");

            String[] lines =
                    text.split("\\R");

            int rowNumber = 0;

            for (String line : lines) {

                if (line == null || line.trim().isEmpty()) {
                    continue;
                }

                Row row =
                        sheet.createRow(rowNumber++);

                /*
                 * Try to separate table-like PDF text.
                 *
                 * First try tab separation.
                 */
                String[] columns =
                        line.trim().split("\\t");

                /*
                 * If there are no tabs, try multiple spaces.
                 */
                if (columns.length == 1) {
                    columns =
                            line.trim().split("\\s{2,}");
                }

                /*
                 * If still only one column,
                 * store the complete line in column A.
                 */
                for (int columnNumber = 0;
                     columnNumber < columns.length;
                     columnNumber++) {

                    row.createCell(columnNumber)
                            .setCellValue(
                                    columns[columnNumber].trim()
                            );
                }
            }

            // 8. Automatically adjust column width
            int maxColumns = 0;

            for (Row row : sheet) {
                maxColumns =
                        Math.max(
                                maxColumns,
                                row.getLastCellNum()
                        );
            }

            for (int i = 0; i < maxColumns; i++) {
                sheet.autoSizeColumn(i);
            }

            // 9. Save Excel file
            try (FileOutputStream outputStream =
                         new FileOutputStream(
                                 excelPath.toFile()
                         )) {

                workbook.write(outputStream);
            }
        }

        System.out.println(
                "===================================="
        );
        System.out.println(
                "PDF TO EXCEL SUCCESS"
        );
        System.out.println(
                "PDF ID       : " + id
        );
        System.out.println(
                "PDF Path     : " + pdfPath
        );
        System.out.println(
                "Excel Path   : " + excelPath
        );
        System.out.println(
                "Excel Exists : " + Files.exists(excelPath)
        );
        System.out.println(
                "===================================="
        );

        return excelPath.toString();
    }
}
