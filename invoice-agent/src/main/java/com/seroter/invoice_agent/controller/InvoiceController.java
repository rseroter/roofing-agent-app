package com.seroter.invoice_agent.controller;

import com.seroter.invoice_agent.service.AgentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InvoiceController {

    private static final Logger logger = LoggerFactory.getLogger(InvoiceController.class);
    private final AgentService agentService;

    public InvoiceController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/generateinvoice")
    public ResponseEntity<String> generateInvoiceEndpoint(@RequestBody String conversationJson) {
        if (conversationJson == null || conversationJson.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Conversation history (JSON string) cannot be null or empty.");
        }

        try {
            logger.info("Triggering agent to generate invoice...");
            String prompt = "Generate and publish a PDF invoice for our roofing customer based on this appointment conversation. " + conversationJson;
            agentService.run(prompt);
            return ResponseEntity.ok("Invoice generation process initiated successfully. Check server logs for details and GCS path.");
        } catch (Exception e) {
            logger.error("An unexpected error occurred during invoice generation", e);
            return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
        }
    }
}