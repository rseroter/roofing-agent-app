package com.seroter.invoice_agent.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.agents.LlmAgent;
import com.google.adk.agents.SequentialAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.BaseTool;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import com.seroter.invoice_agent.config.InvoiceAgentProperties;
import io.reactivex.rxjava3.core.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

    private final SequentialAgent invoiceAgent;
    private final InvoiceAgentProperties properties;

    public AgentService(
            PdfTool pdfTool,
            InvoiceAgentProperties properties,
            @Value("${mcp.toolbox.url}") String mcpServerUrl,
            @Value("classpath:prompts/html_generator_agent.txt") Resource htmlGeneratorPrompt,
            @Value("classpath:prompts/best_offer_agent.txt") Resource bestOfferPrompt,
            @Value("classpath:prompts/pdf_writer_agent.txt") Resource pdfWriterPrompt
    ) {
        this.properties = properties;
        this.invoiceAgent = createInvoiceAgent(pdfTool, mcpServerUrl, htmlGeneratorPrompt, bestOfferPrompt, pdfWriterPrompt);
    }

    private String resourceToString(Resource resource) {
        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private SequentialAgent createInvoiceAgent(
            PdfTool pdfTool,
            String mcpServerUrl,
            Resource htmlGeneratorPrompt,
            Resource bestOfferPrompt,
            Resource pdfWriterPrompt
    ) {
        String modelName = properties.getAgent().getModelName();

        LlmAgent htmlGeneratorAgent = LlmAgent.builder().model(modelName).name("htmlGeneratorAgent").description("Generates an HTML invoice from conversation data.").instruction(resourceToString(htmlGeneratorPrompt)).outputKey("invoicehtml").build();

        List<BaseTool> mcpTools = loadMcpTools(mcpServerUrl);

        LlmAgent bestOfferAgent = LlmAgent.builder().model(modelName).name("bestOfferAgent").description("Applies the best offers available to the invoice").instruction(resourceToString(bestOfferPrompt)).tools(mcpTools).outputKey("bestinvoicehtml").build();

        FunctionTool generatePdfTool = FunctionTool.create(PdfTool.class, "generatePdfFromHtml");

        LlmAgent pdfWriterAgent = LlmAgent.builder().model(modelName).name("pdfWriterAgent").description("Creates a PDF from HTML and saves it to cloud storage.").instruction(resourceToString(pdfWriterPrompt)).tools(List.of(generatePdfTool)).build();

        return SequentialAgent.builder().name(properties.getAgent().getAppName()).description("Execute the complete sequence to generate, improve, and publish an PDF invoice to Google Cloud Storage.").subAgents(htmlGeneratorAgent, bestOfferAgent, pdfWriterAgent).build();
    }

    private List<BaseTool> loadMcpTools(String mcpServerUrl) {
        try {
            SseServerParameters params = SseServerParameters.builder().url(mcpServerUrl).build();
            logger.info("Initializing MCP toolset with params: {}", params);
            McpToolset.McpToolsAndToolsetResult result = McpToolset.fromServer(params, new ObjectMapper()).get();
            if (result.getTools() != null && !result.getTools().isEmpty()) {
                logger.info("MCP tools loaded: {}", result.getTools().size());
                return result.getTools().stream().map(mcpTool -> (BaseTool) mcpTool).collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error initializing MCP toolset", e);
        }
        return new ArrayList<>();
    }

    public void run(String prompt) {
        String appName = properties.getAgent().getAppName();
        String userId = properties.getAgent().getUserId();

        InMemoryRunner runner = new InMemoryRunner(invoiceAgent, appName);
        Session session = runner.sessionService().createSession(appName, userId).blockingGet();
        Content userMessage = Content.fromParts(Part.fromText(prompt));

        logger.info("Running sequential agent...");

        Flowable<Event> eventStream = runner.runAsync(userId, session.id(), userMessage);
        eventStream.blockingForEach(event -> {
            if (event.finalResponse()) {
                logger.info("Final response from agent:\n{}", event.stringifyContent());
            }
        });
    }
}