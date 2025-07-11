package com.seroter.invoice_agent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "invoice-agent")
public class InvoiceAgentProperties {
    private Gcs gcs = new Gcs();
    private Agent agent = new Agent();

    public Gcs getGcs() {
        return gcs;
    }

    public void setGcs(Gcs gcs) {
        this.gcs = gcs;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public static class Gcs {
        private String projectId;
        private String bucketName;

        public String getProjectId() { return projectId; }
        public void setProjectId(String projectId) { this.projectId = projectId; }
        public String getBucketName() { return bucketName; }
        public void setBucketName(String bucketName) { this.bucketName = bucketName; }
    }

    public static class Agent {
        private String appName;
        private String userId;
        private String modelName;

        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String getModelName() { return modelName; }
        public void setModelName(String modelName) { this.modelName = modelName; }
    }
}