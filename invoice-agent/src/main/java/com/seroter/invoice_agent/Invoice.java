package com.seroter.invoice_agent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class Invoice {
    private String invoiceNumber;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String customerName;
    private String customerAddress;
    private List<InvoiceItem> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount; // Example: could be a percentage or fixed
    private String summary;
    private BigDecimal totalAmount;

    // Constructors
    public Invoice() {
    }

    // Getters and Setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItem> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public BigDecimal getTotalAmount() {
        // In a real app, this would be calculated from items, subtotal, tax, etc.
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    // Method to calculate totals can be added here
    public void calculateTotals() {
        if (this.items == null || this.items.isEmpty()) {
            this.subtotal = BigDecimal.ZERO;
            this.taxAmount = BigDecimal.ZERO;
            this.totalAmount = BigDecimal.ZERO;
            return;
        }

        // Basic example: sum of item line totals
        this.subtotal = items.stream()
                             .map(InvoiceItem::getLineTotal)
                             // Filter out potential nulls, though InvoiceItem aims to prevent them
                             .filter(java.util.Objects::nonNull)
                             .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Add tax calculation if needed
        this.taxAmount = BigDecimal.ZERO; // Placeholder
        this.totalAmount = this.subtotal.add(this.taxAmount);
    }
}