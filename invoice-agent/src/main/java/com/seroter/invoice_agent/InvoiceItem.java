package com.seroter.invoice_agent;

import java.math.BigDecimal;

public class InvoiceItem {
    private String description;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;

    // Constructors
    public InvoiceItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and Setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.lineTotal = this.unitPrice.multiply(BigDecimal.valueOf(quantity)); // Recalculate if quantity changes
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(this.quantity)); // Recalculate if unit price changes
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    // lineTotal is typically calculated, so a public setter might not be needed
    // or should trigger recalculation of parent invoice totals.
}