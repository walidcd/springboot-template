package com.example.demo;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;

@RestController
@RequestMapping("/facturx")
public class FacturXController {

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

    @PostMapping(value = "/attach", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> attachXmlToPdf(
            @RequestParam("pdf") MultipartFile pdfFile,
            @RequestParam("xml") MultipartFile xmlFile
    ) throws Exception {
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

    // Utility: Parse XML and return InvoiceData
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

        // Extract Date
        String date = xpath.evaluate("//*[local-name()='IssueDateTime']/*[local-name()='DateTimeString']", doc);
        data.setDate(date);

        // Extract Amount
        String amount = xpath.evaluate("//*[local-name()='GrandTotalAmount']", doc);
        if (amount != null && !amount.isEmpty())
            data.setAmount(new BigDecimal(amount));
        else
            data.setAmount(BigDecimal.ZERO);

        return data;
    }

    // Use PDFBox template for invoice design
    private byte[] generatePdf(InvoiceData data) throws Exception {
        // Load XSL-FO template as string
        String foTemplate = new String(getClass().getResourceAsStream("/invoice-template.fo").readAllBytes(), "UTF-8");

        // Replace placeholders with data
        foTemplate = foTemplate.replace("{{INVOICE_NUMBER}}", data.getInvoiceNumber());
        foTemplate = foTemplate.replace("{{CUSTOMER}}", data.getCustomerName());
        foTemplate = foTemplate.replace("{{DATE}}", data.getDate());
        foTemplate = foTemplate.replace("{{AMOUNT}}", data.getAmount().toString());

        // Convert filled FO to InputStream
        ByteArrayInputStream foInput = new ByteArrayInputStream(foTemplate.getBytes("UTF-8"));
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();

        // FOP setup
        org.apache.fop.apps.FopFactory fopFactory = org.apache.fop.apps.FopFactory.newInstance(new File(".").toURI());
        org.apache.fop.apps.FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        org.apache.fop.apps.Fop fop = fopFactory.newFop(org.apache.xmlgraphics.util.MimeConstants.MIME_PDF, foUserAgent, pdfOut);

        javax.xml.transform.TransformerFactory factory = javax.xml.transform.TransformerFactory.newInstance();
        javax.xml.transform.Transformer transformer = factory.newTransformer();

        javax.xml.transform.Source src = new javax.xml.transform.stream.StreamSource(foInput);
        javax.xml.transform.Result res = new javax.xml.transform.sax.SAXResult(fop.getDefaultHandler());

        transformer.transform(src, res);

        return pdfOut.toByteArray();
    }


    // Embed XML into PDF as Factur-X (ZUGFeRD)
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
