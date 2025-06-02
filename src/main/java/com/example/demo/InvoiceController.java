package com.example.demo;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;

@Controller
public class InvoiceController {

    @GetMapping("/")
    public String index() {
        return "upload";
    }

    @PostMapping("/upload")
    public ResponseEntity<ByteArrayResource> handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            // Parse XML to extract invoice data
            InvoiceData invoiceData = parseXml(file);

            // Generate PDF with invoice data
            byte[] pdfBytes = generatePdf(invoiceData);

            // Embed original XML into PDF to create Factur-X
            byte[] facturxPdf = embedXmlIntoPdf(pdfBytes, file.getBytes());

            // Return the Factur-X PDF as a download
            ByteArrayResource resource = new ByteArrayResource(facturxPdf);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_facturx.pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(facturxPdf.length)
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    private InvoiceData parseXml(MultipartFile file) throws Exception {
        // Implement XML parsing logic to extract invoice data
        // For demonstration, returning dummy data
        InvoiceData data = new InvoiceData();
        data.setInvoiceNumber("INV-1001");
        data.setCustomerName("John Doe");
        data.setAmount(new BigDecimal("123.45"));
        return data;
    }

    private byte[] generatePdf(InvoiceData data) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Invoice Number: " + data.getInvoiceNumber()));
        document.add(new Paragraph("Customer Name: " + data.getCustomerName()));
        document.add(new Paragraph("Amount: " + data.getAmount()));
        document.close();
        return baos.toByteArray();
    }

    private byte[] embedXmlIntoPdf(byte[] pdfBytes, byte[] xmlBytes) throws Exception {
        // Save PDF bytes to a temporary file
        File tempPdf = File.createTempFile("invoice", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempPdf)) {
            fos.write(pdfBytes);
        }

        // Use Mustangproject to embed XML into PDF
        ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3()
                .ignorePDFAErrors()
                .load(tempPdf.getAbsolutePath())
                .setXML(xmlBytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.export(outputStream);

        // Delete temporary file
        tempPdf.delete();

        return outputStream.toByteArray();
    }
}
