package com.smartpdf.backend.controller;

import com.smartpdf.backend.entity.PdfFile;
import com.smartpdf.backend.repository.PdfRepository;
import com.smartpdf.backend.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.FileOutputStream;
import java.util.Map;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.core.io.InputStreamResource;
import com.smartpdf.backend.entity.CustomFormField;
import com.smartpdf.backend.service.PdfFormFillService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import com.smartpdf.backend.dto.PdfFormFillRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartpdf.backend.service.PdfBookmarkService;
@RestController
@RequestMapping("/api/pdfs")
@CrossOrigin(origins = "*")
public class PdfController {

    @Autowired
    private PdfService pdfService;
    @Autowired
    private PdfFormFillService pdfFormFillService;
    @Autowired
    private PdfTextService pdfTextService;

    @Autowired
    private PdfEditService pdfEditService;

    @Autowired
    private PdfModifyService pdfModifyService;
    @Autowired
    private PdfHighlightService pdfHighlightService;
    @Autowired
    private PdfWordHighlightService pdfWordHighlightService;
    @Autowired
    private PdfMergeService pdfMergeService;
    @Autowired
    private PdfSplitService pdfSplitService;
    @Autowired
    private PdfDeleteService pdfDeleteService;
    @Autowired
    private PdfRotateService pdfRotateService;
    @Autowired
    private PdfExtractPagesService pdfExtractPagesService;
    @Autowired
    private PdfReorderService pdfReorderService;
    @Autowired
    private PdfPageNumberService pdfPageNumberService;
    @Autowired
    private PdfWatermarkService pdfWatermarkService;
    @Autowired
    private PdfPasswordService pdfPasswordService;
    @Autowired
    private PdfPasswordRemoveService pdfPasswordRemoveService;
    @Autowired
    private PdfMetadataService pdfMetadataService;
    @Autowired
    private PdfCompressionService pdfCompressionService;
    @Autowired
    private PdfToImageService pdfToImageService;
    @Autowired
    private ImageToPdfService imageToPdfService;
    @Autowired
    private PdfPermissionService pdfPermissionService;
    @Autowired
    private PdfThumbnailService pdfThumbnailService;
    @Autowired
    private PdfSignatureService pdfSignatureService;
    @Autowired
    private PdfToWordService pdfToWordService;
    @Autowired
    private WordToPdfService wordToPdfService;
    @Autowired
    private PdfRepository pdfRepository;
    @Autowired
    private PdfToExcelService pdfToExcelService;
    @Autowired
    private PdfSearchService pdfSearchService;
    @Autowired
    private PdfOcrService pdfOcrService;
    @Autowired
    private PdfFindReplaceService pdfFindReplaceService;
    @Autowired
    private PdfFormFillerService pdfFormFillerService;
    @Autowired
    private CustomFormFieldService customFormFieldService;
@Autowired
private ObjectMapper objectMapper;
    @Autowired
    private PdfBookmarkService pdfBookmarkService;
    //upload
  @PostMapping(
          value = "/upload",
          consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<?> uploadPdf(
          @RequestParam(value = "file", required = false) MultipartFile file) {

      System.out.println("=================================");
      System.out.println("UPLOAD ENDPOINT CALLED");

      try {

          if (file == null) {
              System.out.println("FILE IS NULL");
              return ResponseEntity
                      .badRequest()
                      .body("No file received. The form field must be named 'file'.");
          }

          System.out.println("Received file: " + file.getOriginalFilename());
          System.out.println("File size: " + file.getSize());
          System.out.println("Content type: " + file.getContentType());

          if (file.isEmpty()) {
              return ResponseEntity
                      .badRequest()
                      .body("Selected file is empty.");
          }

          PdfFile uploadedPdf = pdfService.uploadPdf(file);

          System.out.println("UPLOAD SUCCESS");
          System.out.println("Database ID: " + uploadedPdf.getId());
          System.out.println("Saved path: " + uploadedPdf.getFilePath());

          return ResponseEntity.ok(uploadedPdf);

      } catch (Exception e) {

          System.out.println("=================================");
          System.out.println("UPLOAD ERROR");
          System.out.println("Message: " + e.getMessage());
          e.printStackTrace();
          System.out.println("=================================");

          return ResponseEntity
                  .status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body("Upload failed: " + e.getMessage());
      }
  }

    // Get all PDFs
    @GetMapping
    public ResponseEntity<List<PdfFile>> getAllPdfs() {
        return ResponseEntity.ok(pdfService.getAllPdfs());
    }

    // Get PDF by ID
    @GetMapping("/{id}")
    public ResponseEntity<PdfFile> getPdfById(@PathVariable Long id) {
        PdfFile pdf = pdfService.getPdfById(id);
        if (pdf == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(pdf);
    }

    // Download PDF
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Long id) {
        try {
            PdfFile pdf = pdfService.getPdfById(id);
            if (pdf == null) {
                return ResponseEntity.notFound().build();
            }

            Path filePath = Paths.get(pdf.getFilePath());
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + pdf.getFileName() + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    // Download generated PDF by file name
    @GetMapping("/download-file/{fileName:.+}")
    public ResponseEntity<Resource> downloadGeneratedPdf(
            @PathVariable String fileName) {

        try {

            Path filePath = Paths.get("uploads")
                    .resolve(fileName)
                    .normalize();

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    // Download merged PDF
    @GetMapping("/download-merged/{fileName}")
    public ResponseEntity<Resource> downloadMergedPdf(
            @PathVariable String fileName) {

        try {

            Path filePath = Paths.get("uploads").resolve(fileName).normalize();

            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(filePath);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + fileName + "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
    // ============================================================
// Preview PDF
// ============================================================

    @GetMapping("/view/{id}")
    public ResponseEntity<Resource> viewPdf(
            @PathVariable Long id) {

        try {

            System.out.println("=================================");
            System.out.println("VIEW PDF REQUEST");
            System.out.println("PDF ID = " + id);

            PdfFile pdf =
                    pdfService.getPdfById(id);

            // ----------------------------------------------------
            // Check database record
            // ----------------------------------------------------

            if (pdf == null) {

                System.out.println(
                        "PDF NOT FOUND IN DATABASE: " + id
                );

                return ResponseEntity
                        .notFound()
                        .build();
            }


            System.out.println(
                    "PDF FILE NAME = "
                            + pdf.getFileName()
            );

            System.out.println(
                    "PDF FILE PATH = "
                            + pdf.getFilePath()
            );


            // ----------------------------------------------------
            // Check file path
            // ----------------------------------------------------

            if (pdf.getFilePath() == null ||
                    pdf.getFilePath().trim().isEmpty()) {

                System.out.println(
                        "PDF FILE PATH IS EMPTY"
                );

                return ResponseEntity
                        .status(
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                        .build();
            }


            Path filePath =
                    Paths.get(
                            pdf.getFilePath()
                    ).toAbsolutePath().normalize();


            System.out.println(
                    "ABSOLUTE FILE PATH = "
                            + filePath
            );


            // ----------------------------------------------------
            // Check physical file
            // ----------------------------------------------------

            if (!Files.exists(filePath)) {

                System.out.println(
                        "PDF FILE DOES NOT EXIST:"
                );

                System.out.println(
                        filePath
                );

                return ResponseEntity
                        .notFound()
                        .build();
            }


            if (!Files.isRegularFile(filePath)) {

                System.out.println(
                        "PATH IS NOT A FILE:"
                );

                System.out.println(
                        filePath
                );

                return ResponseEntity
                        .notFound()
                        .build();
            }


            // ----------------------------------------------------
            // Create resource
            // ----------------------------------------------------

            Resource resource =
                    new FileSystemResource(
                            filePath
                    );


            // ----------------------------------------------------
            // Return PDF inline
            // ----------------------------------------------------

            return ResponseEntity
                    .ok()
                    .contentType(
                            MediaType.APPLICATION_PDF
                    )
                    .contentLength(
                            Files.size(filePath)
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" +
                                    pdf.getFileName() +
                                    "\""
                    )
                    .body(resource);


        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .build();
        }
    }

    // Extract text from PDF
    @GetMapping("/text/{id}")
    public ResponseEntity<String> extractPdfText(@PathVariable Long id) {
        try {
            PdfFile pdf = pdfService.getPdfById(id);
            if (pdf == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PDF not found");
            }

            String text = pdfTextService.extractText(pdf.getFilePath());
            return ResponseEntity.ok(text);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error extracting PDF text: " + e.getMessage());
        }
    }

    // Create edited PDF from text
    @PostMapping("/edit")
    public ResponseEntity<String> createEditedPdf(
            @RequestParam("text") String text,
            @RequestParam("fileName") String fileName) {
        try {
            String savedPath = pdfEditService.createEditedPdf(text, fileName);
            return ResponseEntity.ok("Edited PDF created successfully at: " + savedPath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating edited PDF: " + e.getMessage());
        }
    }
    @PostMapping("/add-text/{id}")
    public ResponseEntity<String> addTextToPdf(
            @PathVariable Long id,
            @RequestParam("text") String text,
            @RequestParam("pageNumber") int pageNumber,
            @RequestParam("x") float x,
            @RequestParam("y") float y,
            @RequestParam("fontSize") float fontSize,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String savedPath = pdfModifyService.addTextToPdf(
                    id,
                    text,
                    pageNumber,
                    x,
                    y,
                    fontSize,
                    outputFileName
            );

            return ResponseEntity.ok(
                    "Text added successfully. PDF saved at: " + savedPath
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding text to PDF: " + e.getMessage());
        }
    }
    // Delete PDF
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePdf(@PathVariable Long id) {
        try {
            PdfFile pdf = pdfService.getPdfById(id);
            if (pdf == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("PDF not found");
            }

            pdfService.deletePdf(id);
            return ResponseEntity.ok("PDF deleted successfully");

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting PDF: " + e.getMessage());
        }
    }
    @PostMapping("/highlight/{id}")
    public ResponseEntity<String> highlightArea(
            @PathVariable Long id,
            @RequestParam("pageNumber") int pageNumber,
            @RequestParam("x") float x,
            @RequestParam("y") float y,
            @RequestParam("width") float width,
            @RequestParam("height") float height,
            @RequestParam("outputFileName") String outputFileName) {

        try {
            String savedPath = pdfHighlightService.highlightArea(
                    id, pageNumber, x, y, width, height, outputFileName
            );
            return ResponseEntity.ok("Highlighted PDF saved at: " + savedPath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error highlighting PDF: " + e.getMessage());
        }
    }
    @PostMapping("/highlight-word/{id}")
    public ResponseEntity<String> highlightWord(
            @PathVariable Long id,
            @RequestParam("word") String word,
            @RequestParam("outputFileName") String outputFileName) {

        try {
            String savedPath = pdfWordHighlightService.highlightWord(id, word, outputFileName);
            return ResponseEntity.ok("Word highlighted PDF saved at: " + savedPath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error highlighting word: " + e.getMessage());
        }
    }
    @PostMapping("/merge")
    public ResponseEntity<String> mergePdfs(
            @RequestParam("pdfIds") String pdfIds,
            @RequestParam("outputFileName") String outputFileName) {

        try {
            String savedPath = pdfMergeService.mergePdfs(pdfIds, outputFileName);
            return ResponseEntity.ok("Merged PDF saved at: " + savedPath);
        }  catch (Exception e) {
        e.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(null);
    }

    }
    @PostMapping("/split/{id}")
    public ResponseEntity<?> splitPdf(
            @PathVariable("id") Long id,
            @RequestParam("pages") String pages,
            @RequestParam("outputPrefix") String outputPrefix) {

        try {

            System.out.println("========== SPLIT PDF ==========");
            System.out.println("PDF ID       : " + id);
            System.out.println("Pages        : " + pages);
            System.out.println("Output Prefix: " + outputPrefix);

            List<String> result =
                    pdfSplitService.splitPdf(
                            id,
                            pages,
                            outputPrefix
                    );

            System.out.println("Split Result : " + result);

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Error splitting PDF: " + e.getMessage());
        }
    }
    @PostMapping("/delete-pages/{id}")
    public ResponseEntity<?> deletePages(
            @PathVariable("id") Long id,
            @RequestParam("pages") String pages,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfDeleteService.deletePages(
                    id,
                    pages,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error deleting pages: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/bookmarks/{pdfId}")
    public ResponseEntity<?> addBookmark(
            @PathVariable Long pdfId,

            @RequestParam String bookmarkTitle,

            @RequestParam Integer pageNumber,

            @RequestParam(
                    value = "outputFileName",
                    required = false
            ) String outputFileName
    ) {

        try {

            if (outputFileName == null ||
                    outputFileName.trim().isEmpty()) {

                outputFileName =
                        "bookmarked-" + pdfId + ".pdf";
            }

            String savedPath =
                    pdfBookmarkService.addBookmark(
                            pdfId,
                            bookmarkTitle,
                            pageNumber,
                            outputFileName
                    );

            String fileName =
                    Paths.get(savedPath)
                            .getFileName()
                            .toString();

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "Bookmark added successfully",

                            "fileName",
                            fileName,

                            "downloadUrl",
                            "/api/pdfs/download-file/"
                                    + fileName
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "Error adding bookmark: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/rotate/{id}")
    public ResponseEntity<?> rotatePdf(
            @PathVariable("id") Long id,
            @RequestParam("pages") String pages,
            @RequestParam("rotation") int rotation,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfRotateService.rotatePdf(
                    id,
                    pages,
                    rotation,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error rotating PDF: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/extract-pages/{id}")
    public ResponseEntity<?> extractPages(
            @PathVariable("id") Long id,
            @RequestParam("pages") String pages,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfExtractPagesService.extractPages(
                    id,
                    pages,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error extracting pages: "
                                    + e.getMessage()
                    );
        }

    }
    @PostMapping("/reorder/{id}")
    public ResponseEntity<?> reorderPages(
            @PathVariable("id") Long id,
            @RequestParam("pageOrder") String pageOrder,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfReorderService.reorderPages(
                    id,
                    pageOrder,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error reordering pages: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/add-page-numbers/{id}")
    public ResponseEntity<?> addPageNumbers(
            @PathVariable("id") Long id,
            @RequestParam("startingPageNumber") int startingPageNumber,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfPageNumberService.addPageNumbers(
                    id,
                    startingPageNumber,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error adding page numbers: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/watermark/{id}")
    public ResponseEntity<?> addWatermark(
            @PathVariable Long id,
            @RequestParam("watermarkText") String watermarkText,
            @RequestParam("outputFileName") String outputFileName) {

        try {
            String result = pdfWatermarkService.addWatermark(
                    id,
                    watermarkText,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error adding watermark: " + e.getMessage());
        }
    }
    @PostMapping("/protect/{id}")
    public ResponseEntity<?> protectPdf(
            @PathVariable("id") Long id,
            @RequestParam("password") String password,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfPasswordService.protectPdf(
                    id,
                    password,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error protecting PDF: "
                                    + e.getMessage()
                    );
        }

    }
    @PostMapping("/remove-password/{id}")
    public ResponseEntity<?> removePassword(
            @PathVariable("id") Long id,
            @RequestParam("password") String password,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfPasswordRemoveService.removePassword(
                    id,
                    password,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error removing password: "
                                    + e.getMessage()
                    );
        }

    }
    @PostMapping("/metadata/{id}")
    public ResponseEntity<?> editMetadata(
            @PathVariable("id") Long id,
            @RequestParam("title") String title,
            @RequestParam("author") String author,
            @RequestParam("subject") String subject,
            @RequestParam("keywords") String keywords,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfMetadataService.editMetadata(
                    id,
                    title,
                    author,
                    subject,
                    keywords,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body("Error editing metadata: " + e.getMessage());
        }
    }
    @PostMapping("/compress/{id}")
    public ResponseEntity<?> compressPdf(
            @PathVariable("id") Long id,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfCompressionService.compressPdf(
                    id,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error compressing PDF: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/pdf-to-images/{id}")
    public ResponseEntity<?> convertPdfToImages(
            @PathVariable("id") Long id,
            @RequestParam("outputFolder") String outputFolder) {

        try {

            String result = pdfToImageService.convertPdfToImages(
                    id,
                    outputFolder
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error converting PDF to images: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/images-to-pdf")
    public ResponseEntity<?> imagesToPdf(
            @RequestParam("images") MultipartFile[] images,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = imageToPdfService.convertImagesToPdf(
                    images,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error converting images to PDF: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/permissions/{id}")
    public ResponseEntity<?> protectWithPermissions(
            @PathVariable("id") Long id,
            @RequestParam("ownerPassword") String ownerPassword,
            @RequestParam("userPassword") String userPassword,
            @RequestParam("allowPrinting") boolean allowPrinting,
            @RequestParam("allowCopying") boolean allowCopying,
            @RequestParam("allowModification") boolean allowModification,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfPermissionService.protectWithPermissions(
                    id,
                    ownerPassword,
                    userPassword,
                    allowPrinting,
                    allowCopying,
                    allowModification,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error setting PDF permissions: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/thumbnails/{id}")
    public ResponseEntity<?> generateThumbnails(
            @PathVariable("id") Long id,
            @RequestParam("outputFolder") String outputFolder) {

        try {

            String result = pdfThumbnailService.generateThumbnails(
                    id,
                    outputFolder
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error generating thumbnails: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/to-word/{id}")
    public ResponseEntity<?> convertToWord(
            @PathVariable Long id,
            @RequestParam("outputFileName")
            String outputFileName
    ) {

        try {
            String result =
                    pdfToWordService.convertPdfToWord(
                            id,
                            outputFileName
                    );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "Error converting PDF to Word: "
                                    + e.getMessage()
                    );

        }

    }

    @PostMapping("/signature/{id}")
    public ResponseEntity<?> addSignature(
            @PathVariable("id") Long id,
            @RequestParam("signature") MultipartFile signature,
            @RequestParam("pageNumber") int pageNumber,
            @RequestParam("x") float x,
            @RequestParam("y") float y,
            @RequestParam("width") float width,
            @RequestParam("height") float height,
            @RequestParam("outputFileName") String outputFileName) {

        try {

            String result = pdfSignatureService.addSignature(
                    id,
                    signature,
                    pageNumber,
                    x,
                    y,
                    width,
                    height,
                    outputFileName
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            "Error adding signature: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping(
            value = "/excel-to-pdf",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<?> excelToPdf(
            @RequestParam("file") MultipartFile file) {

        try {

            System.out.println("=================================");
            System.out.println("EXCEL TO PDF REQUEST RECEIVED");
            System.out.println("File: " +
                    (file != null
                            ? file.getOriginalFilename()
                            : "NULL"));
            System.out.println("=================================");

            if (file == null || file.isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Excel file is empty");
            }

            byte[] pdfBytes =
                    pdfService.convertExcelToPdf(file);

            System.out.println(
                    "Excel converted successfully"
            );

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"converted-excel.pdf\""
                    )
                    .contentType(
                            MediaType.APPLICATION_PDF
                    )
                    .body(pdfBytes);

        } catch (Exception e) {

            e.printStackTrace();

            System.out.println(
                    "EXCEL TO PDF FAILED: "
                            + e.getMessage()
            );

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "Excel to PDF conversion failed: "
                                    + e.getMessage()
                    );
        }
    }
    @PostMapping("/upload-word")
    public ResponseEntity<?> uploadWord(
            @RequestParam("file") MultipartFile file) {

        try {

            // Check file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please select a Word file");
            }

            // Get filename
            String fileName = file.getOriginalFilename();

            if (fileName == null || fileName.isBlank()) {
                return ResponseEntity.badRequest()
                        .body("Invalid file name");
            }

            // Check Word extension
            String lowerName = fileName.toLowerCase();

            if (!lowerName.endsWith(".docx")
                    && !lowerName.endsWith(".doc")) {

                return ResponseEntity.badRequest()
                        .body("Only .doc and .docx files are allowed");
            }

            // Create uploads folder
            Path uploadDir = Paths
                    .get(System.getProperty("user.dir"))
                    .resolve("uploads")
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(uploadDir);

            // Prevent folder/path problems
            fileName = Paths.get(fileName)
                    .getFileName()
                    .toString();

            // File location
            Path filePath = uploadDir.resolve(fileName);

            // Save Word file
            file.transferTo(filePath.toFile());

            // Save database record
            PdfFile pdfFile = new PdfFile();

            pdfFile.setFileName(fileName);

            pdfFile.setFilePath(
                    filePath.toAbsolutePath().toString()
            );

            pdfFile.setFileSize(
                    Files.size(filePath)
            );

            PdfFile saved =
                    pdfRepository.save(pdfFile);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Word upload failed: "
                                    + e.getMessage()
                    );
        }
    }


    @GetMapping("/download-images")
    public ResponseEntity<Resource> downloadImages(
            @RequestParam("folder") String folder) {

        try {

            Path folderPath = Paths.get(folder).normalize();

            if (!Files.exists(folderPath) ||
                    !Files.isDirectory(folderPath)) {

                folderPath = Paths.get("uploads")
                        .resolve(folder)
                        .normalize();
            }

            if (!Files.exists(folderPath) ||
                    !Files.isDirectory(folderPath)) {

                return ResponseEntity.notFound().build();
            }

            Path zipPath = Paths.get("uploads")
                    .resolve("pdf_images.zip")
                    .normalize();

            try (ZipOutputStream zipOut =
                         new ZipOutputStream(
                                 new FileOutputStream(zipPath.toFile()))) {

                try (var files = Files.list(folderPath)) {

                    files.filter(Files::isRegularFile)
                            .forEach(file -> {

                                try {

                                    ZipEntry entry =
                                            new ZipEntry(
                                                    file.getFileName().toString()
                                            );

                                    zipOut.putNextEntry(entry);

                                    Files.copy(file, zipOut);

                                    zipOut.closeEntry();

                                } catch (IOException e) {

                                    throw new RuntimeException(e);

                                }

                            });
                }
            }

            Resource resource =
                    new FileSystemResource(zipPath);

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(
                                    "application/zip"
                            )
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"pdf_images.zip\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
        @PostMapping("/to-excel/{id}")
        public ResponseEntity<?> convertToExcel(
                @PathVariable Long id,
                @RequestParam(value = "outputFileName", required = false)
                        String outputFileName) {

            try {

                System.out.println("=================================");
                System.out.println("PDF TO EXCEL REQUEST RECEIVED");
                System.out.println("PDF ID: " + id);
                System.out.println("Output File: " + outputFileName);
                System.out.println("=================================");

                if (outputFileName == null || outputFileName.isBlank()) {
                    outputFileName = "converted.xlsx";
                }

                String excelPath =
                        pdfToExcelService.convertPdfToExcel(
                                id,
                                outputFileName
                        );

                Path filePath = Paths.get(excelPath);

                if (!Files.exists(filePath)) {

                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Excel file was created but could not be found: "
                                    + excelPath);
                }

                if (!Files.isRegularFile(filePath)) {

                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Generated Excel path is not a file: "
                                    + excelPath);
                }

                Resource resource =
                        new FileSystemResource(filePath);

                String downloadName =
                        Paths.get(outputFileName)
                                .getFileName()
                                .toString();

                if (!downloadName.toLowerCase().endsWith(".xlsx")) {
                    downloadName += ".xlsx";
                }

                System.out.println("=================================");
                System.out.println("PDF TO EXCEL SUCCESS");
                System.out.println("Excel Path: " + excelPath);
                System.out.println("Download Name: " + downloadName);
                System.out.println("=================================");

                return ResponseEntity.ok()
                        .contentType(
                                MediaType.parseMediaType(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                                )
                        )
                        .header(
                                HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + downloadName + "\""
                        )
                        .body(resource);

            } catch (Exception e) {

                e.printStackTrace();

                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(
                                "PDF to Excel conversion failed: "
                                        + e.getMessage()
                        );
            }
        }
// =========================================================
// SEARCH PDF
// =========================================================

    @GetMapping("/search/{id}")
    public ResponseEntity<?> searchPdf(
            @PathVariable("id") Long id,
            @RequestParam("keyword") String keyword) {

        try {

            System.out.println("=================================");
            System.out.println("PDF SEARCH REQUEST RECEIVED");
            System.out.println("PDF ID : " + id);
            System.out.println("Keyword: " + keyword);
            System.out.println("=================================");

            if (keyword == null ||
                    keyword.trim().isEmpty()) {

                return ResponseEntity
                        .badRequest()
                        .body("Search keyword cannot be empty");
            }

            List<Map<String, Object>> results =
                    pdfSearchService.searchPdf(
                            id,
                            keyword
                    );

            return ResponseEntity.ok(results);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(
                            "PDF search failed: "
                                    + e.getMessage()
                    );
        }
    }
// =========================================================
// OCR PDF
// =========================================================

    @PostMapping("/ocr/{id}")
    public ResponseEntity<?> ocrPdf(
            @PathVariable("id") Long id,
            @RequestParam(
                    value = "language",
                    required = false,
                    defaultValue = "eng"
            )
            String language) {

        try {

            System.out.println("=================================");
            System.out.println("PDF OCR REQUEST RECEIVED");
            System.out.println("PDF ID  : " + id);
            System.out.println("Language: " + language);
            System.out.println("=================================");

            /*
             * Find PDF from database.
             */
            PdfFile pdf =
                    pdfService.getPdfById(id);

            if (pdf == null) {

                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("PDF not found");
            }

            /*
             * Validate PDF path.
             */
            if (pdf.getFilePath() == null ||
                    pdf.getFilePath().isBlank()) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                "PDF file path is missing"
                        );
            }

            /*
             * Perform OCR.
             */
            List<Map<String, Object>> results =
                    pdfOcrService.performOcr(
                            pdf.getFilePath(),
                            language
                    );

            /*
             * Return OCR results.
             */
            return ResponseEntity.ok(results);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            "PDF OCR failed: "
                                    + e.getMessage()
                    );
        }

    }
    @PostMapping("/find-replace/{id}")
    public ResponseEntity<?> findAndReplacePdf(
            @PathVariable("id") Long id,
            @RequestParam("searchText") String searchText,
            @RequestParam("replaceText") String replaceText,
            @RequestParam("outputFileName") String outputFileName) {

        System.out.println("=================================");
        System.out.println("FIND & REPLACE REQUEST");
        System.out.println("PDF ID       : " + id);
        System.out.println("Search Text  : " + searchText);
        System.out.println("Replace Text : " + replaceText);
        System.out.println("Output File  : " + outputFileName);
        System.out.println("=================================");

        try {

            if (searchText == null || searchText.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "Search text cannot be empty"
                        )
                );
            }
            searchText = searchText.trim();
            replaceText = replaceText == null ? "" : replaceText.trim();
            outputFileName = outputFileName == null
                    ? "find-replace-result.pdf"
                    : outputFileName.trim();

            if (replaceText == null) {
                replaceText = "";
            }

            if (outputFileName == null || outputFileName.isBlank()) {
                outputFileName = "find-replace-result.pdf";
            }

            if (!outputFileName.toLowerCase().endsWith(".pdf")) {
                outputFileName += ".pdf";
            }

            PdfFile pdf = pdfService.getPdfById(id);

            if (pdf == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of(
                                "success", false,
                                "message", "PDF not found"
                        )
                );
            }

            if (pdf.getFilePath() == null ||
                    pdf.getFilePath().isBlank()) {

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "success", false,
                                "message", "PDF file path is missing"
                        )
                );
            }

            Path inputPath = Paths.get(pdf.getFilePath());

            if (!Files.exists(inputPath)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        Map.of(
                                "success", false,
                                "message",
                                "PDF file does not exist: " + inputPath
                        )
                );
            }

            System.out.println("Input PDF: " + inputPath);
            System.out.println("Starting Find & Replace...");

            String resultPath =
                    pdfFindReplaceService.findAndReplace(
                            pdf.getFilePath(),
                            searchText,
                            replaceText,
                            outputFileName
                    );

            System.out.println("Result: " + resultPath);

            if (resultPath == null || resultPath.isBlank()) {
                return ResponseEntity.status(
                        HttpStatus.INTERNAL_SERVER_ERROR
                ).body(
                        Map.of(
                                "success", false,
                                "message",
                                "Find & Replace returned an empty path"
                        )
                );
            }

            Path resultFile = Paths.get(resultPath);

            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "message",
                            "Find & Replace completed successfully",
                            "outputPath",
                            resultPath,
                            "fileName",
                            resultFile.getFileName().toString()
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of(
                            "success", false,
                            "message", "Find & Replace failed",
                            "error",
                            e.getMessage() == null
                                    ? "Unknown error"
                                    : e.getMessage()
                    )
            );
        }
    }
    @PostMapping("/form-fields/{pdfId}")
    public ResponseEntity<?> createFormField(
            @PathVariable Long pdfId,

            @RequestParam String fieldName,

            @RequestParam String fieldType,

            @RequestParam Integer pageNumber,

            @RequestParam Float x,

            @RequestParam Float y,

            @RequestParam Float width,

            @RequestParam Float height
    ) {

        try {

            CustomFormField field =
                    customFormFieldService.createField(
                            pdfId,
                            fieldName,
                            fieldType,
                            pageNumber,
                            x,
                            y,
                            width,
                            height
                    );

            return ResponseEntity.ok(field);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
    // =========================================================
// GET FORM FIELDS FOR PDF
// =========================================================

    @GetMapping("/form-fields/{pdfId}")
    public ResponseEntity<?> getFormFields(
            @PathVariable Long pdfId) {

        try {

            List<CustomFormField> fields =
                    customFormFieldService.getFields(pdfId);

            return ResponseEntity.ok(fields);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error getting form fields: "
                            + e.getMessage());
        }
    }
 
    // =========================================================
// DOWNLOAD FILLED PDF FORM
// =========================================================

    @GetMapping("/download-form/{fileName:.+}")
    public ResponseEntity<Resource> downloadFilledForm(
            @PathVariable String fileName) {

        try {

            Path uploadDirectory =
                    Paths.get(
                                    System.getProperty("user.dir")
                            )
                            .resolve("uploads")
                            .toAbsolutePath()
                            .normalize();

            Path filePath =
                    uploadDirectory
                            .resolve(fileName)
                            .normalize();

            // Prevent path traversal
            if (!filePath.getParent()
                    .equals(uploadDirectory)) {

                return ResponseEntity
                        .badRequest()
                        .build();
            }

            if (!Files.exists(filePath) ||
                    !Files.isRegularFile(filePath)) {

                return ResponseEntity
                        .notFound()
                        .build();
            }

            Resource resource =
                    new FileSystemResource(filePath);

            return ResponseEntity
                    .ok()
                    .contentType(
                            MediaType.APPLICATION_PDF
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\""
                                    + fileName
                                    + "\""
                    )
                    .body(resource);

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .build();
        }
    }
    @PostMapping("/form-fields/fill/{pdfId}")
    public ResponseEntity<?> fillPdfForm(
            @PathVariable Long pdfId,

            @RequestBody Map<String, Object> request
    ) {

        try {

            Object fieldsObject = request.get("fields");
            List<CustomFormField> fields =
                    objectMapper.convertValue(
                            fieldsObject,
                            new TypeReference<List<CustomFormField>>() {}
                    );



            String signatureImage =
                    (String) request.get("signatureImage");

            String outputFileName =
                    (String) request.get("outputFileName");

            String savedPath =
                    pdfFormFillService.fillPdf(
                            pdfId,
                            fields,
                            signatureImage,
                            outputFileName
                    );

            String fileName =
                    Paths.get(savedPath)
                            .getFileName()
                            .toString();

            return ResponseEntity.ok(
                    Map.of(
                            "message",
                            "PDF filled successfully",

                            "fileName",
                            fileName,

                            "downloadUrl",
                            "/api/pdfs/download-file/"
                                    + fileName
                    )
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            "Error filling PDF: "
                                    + e.getMessage()
                    );
        }
    }
}
