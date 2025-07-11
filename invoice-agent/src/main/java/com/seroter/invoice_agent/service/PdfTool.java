package com.seroter.invoice_agent.service;

import com.google.adk.tools.Annotations.Schema;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.lowagie.text.DocumentException;
import com.seroter.invoice_agent.config.InvoiceAgentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;
import jakarta.annotation.PostConstruct;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PdfTool {

    private static final Logger logger = LoggerFactory.getLogger(PdfTool.class);
    private static final String OUTPUT_PDF_FILENAME = "generated_invoice.pdf";

    private static PdfTool INSTANCE;

    private final InvoiceAgentProperties properties;
    private final Storage storage;

    public PdfTool(InvoiceAgentProperties properties) {
        this.properties = properties;
        this.storage = StorageOptions.newBuilder().setProjectId(properties.getGcs().getProjectId()).build().getService();
    }

    @PostConstruct
    private void init() {
        INSTANCE = this;
    }

    @Schema(name = "generatePdfFromHtml", description = "The full HTML string of the invoice to be converted to a PDF.")
    public static Map<String, Object> generatePdfFromHtml(String htmlContent) throws IOException {
        logger.info("Static generatePdfFromHtml tool called, delegating to instance.");
        return INSTANCE.generatePdfFromHtmlInternal(htmlContent);
    }

    private Map<String, Object> generatePdfFromHtmlInternal(String htmlContent) throws IOException {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("HTML content cannot be null or empty.");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(baos);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
            String uniquePdfFilename = OUTPUT_PDF_FILENAME.replace(".pdf", "_" + timestamp + ".pdf");
            String bucketName = properties.getGcs().getBucketName();

            BlobId blobId = BlobId.of(bucketName, uniquePdfFilename);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("application/pdf").build();

            storage.create(blobInfo, baos.toByteArray());

            String gcsPath = "gs://" + bucketName + "/" + uniquePdfFilename;
            logger.info("Successfully generated PDF and uploaded to GCS: {}", gcsPath);
            return Map.of("status", "success", "file_path", gcsPath);

        } catch (DocumentException e) {
            logger.error("Error during PDF document generation", e);
            throw new IOException("Error during PDF document generation: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error during PDF generation or GCS upload", e);
            throw new IOException("Error during PDF generation or GCS upload: " + e.getMessage(), e);
        }
    }
}