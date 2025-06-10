package com.example.demo;

import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import org.mustangproject.ZUGFeRD.ZUGFeRDExporterFromA3;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

@RestController
@RequestMapping("/facturx")
public class FacturXController {

    // Generate a Factur-X PDF from XML (frontend only sends XML)
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> generateFromXml(@RequestParam("xml") MultipartFile xmlFile) throws Exception {
        InvoiceData invoiceData = parseXml(xmlFile);
        byte[] pdfBytes = generatePdf(invoiceData);
        byte[] facturxPdf = embedXmlIntoPdf(pdfBytes, xmlFile.getBytes());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_facturx.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(facturxPdf);
    }

    // Attach XML to an existing PDF
    @PostMapping(value = "/attach", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> attachXmlToPdf(
            @RequestParam("pdf") MultipartFile pdfFile,
            @RequestParam("xml") MultipartFile xmlFile
    ) throws IOException {
        File tempPdf = File.createTempFile("input", ".pdf");
        pdfFile.transferTo(tempPdf);

        ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3()
                .ignorePDFAErrors()
                .load(tempPdf.getAbsolutePath());
        exporter.setXML(xmlFile.getBytes());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.export(outputStream);

        tempPdf.delete();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=facturx.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(outputStream.toByteArray());
    }

    // --- Utility methods ---

    private InvoiceData parseXml(MultipartFile xmlFile) throws Exception {
        InvoiceData data = new InvoiceData();

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = dBuilder.parse(xmlFile.getInputStream());

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        // Extract Invoice Number
        String invoiceNumber = xpath.evaluate("//*[local-name()='ID']", doc);
        data.setInvoiceNumber(invoiceNumber);

        // Extract Customer Name
        String customerName = xpath.evaluate("//*[local-name()='BuyerTradeParty']/*[local-name()='Name']", doc);
        data.setCustomerName(customerName);

        // Extract Amount
        String amount = xpath.evaluate("//*[local-name()='GrandTotalAmount']", doc);
        if (amount != null && !amount.isEmpty())
            data.setAmount(new BigDecimal(amount));

        return data;
    }

    private byte[] generatePdf(InvoiceData data) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();

        // Load and embed a font for PDF/A compliance
        BaseFont bf = BaseFont.createFont(
                "/Users/walidboutahar/Desktop/projects/springboot-template/arial/ARIAL.TTF",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED
        );
        Font font = new Font(bf, 12);

        document.add(new Paragraph("Invoice Number: " + data.getInvoiceNumber(), font));
        document.add(new Paragraph("Customer Name: " + data.getCustomerName(), font));
        document.add(new Paragraph("Amount: " + data.getAmount(), font));
        document.close();

        return baos.toByteArray();
    }

    private byte[] embedXmlIntoPdf(byte[] pdfBytes, byte[] xmlBytes) throws Exception {
        File tempPdf = File.createTempFile("invoice", ".pdf");
        try (FileOutputStream fos = new FileOutputStream(tempPdf)) {
            fos.write(pdfBytes);
        }

        ZUGFeRDExporterFromA3 exporter = new ZUGFeRDExporterFromA3()
                .ignorePDFAErrors()
                .load(tempPdf.getAbsolutePath())
                .setXML(xmlBytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exporter.export(outputStream);
        tempPdf.delete();

        return outputStream.toByteArray();
    }
}
