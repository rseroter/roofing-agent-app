package com.seroter.invoice_agent;

import com.seroter.invoice_agent.config.InvoiceAgentProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(InvoiceAgentProperties.class)
public class InvoiceAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvoiceAgentApplication.class, args);
	}

}
