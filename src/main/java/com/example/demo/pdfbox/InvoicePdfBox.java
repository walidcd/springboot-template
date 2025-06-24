package com.example.demo.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InvoicePdfBox {

    public static byte[] createInvoice(String invoiceNumber, String customer, String date, String amount) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                // Title
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 22);
                stream.newLineAtOffset(220, 750);
                stream.showText("INVOICE");
                stream.endText();

                // Invoice Info
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(60, 700);
                stream.showText("Invoice Number: " + invoiceNumber);
                stream.endText();

                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(60, 680);
                stream.showText("Date: " + date);
                stream.endText();

                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(60, 660);
                stream.showText("Billed to: " + customer);
                stream.endText();

                // Amount Table
                stream.setLineWidth(0.5f);
                stream.moveTo(60, 640); stream.lineTo(550, 640); stream.stroke();

                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                stream.newLineAtOffset(60, 625);
                stream.showText("Description");
                stream.endText();

                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(400, 625);
                stream.showText("Amount");
                stream.endText();

                stream.moveTo(60, 620); stream.lineTo(550, 620); stream.stroke();

                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(60, 605);
                stream.showText("Service/Product");
                stream.endText();

                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(400, 605);
                stream.showText(amount + " EUR");
                stream.endText();

                stream.moveTo(60, 600); stream.lineTo(550, 600); stream.stroke();

                // Total
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                stream.newLineAtOffset(400, 580);
                stream.showText("Total: " + amount + " EUR");
                stream.endText();
            }

            // Output as byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
}
