package com.example.demo.DAO;

import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class InvoiceData {
    private String invoiceNumber;
    private String invoiceDate;
    private String sellerName;
    private String sellerSiren;
    private String sellerVatId;
    private String buyerName;
    private String buyerSiren;
    private String currency;
    private String buyerReference;
    private String orderReference;
    private BigDecimal grandTotal;
    private BigDecimal taxTotal;
    private BigDecimal dueTotal;
}
