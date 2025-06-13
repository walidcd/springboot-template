package com.example.demo;// In your Spring Controller
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

@RestController
@RequestMapping("/pdfbox-invoice")
public class PdfBoxInvoiceController {

    @GetMapping
    public ResponseEntity<byte[]> getPdfBoxInvoice(
            @RequestParam String invoiceNumber,
            @RequestParam String customer,
            @RequestParam String date,
            @RequestParam String amount
    ) throws Exception {
        byte[] pdf = InvoicePdfBox.createInvoice(invoiceNumber, customer, date, amount);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
