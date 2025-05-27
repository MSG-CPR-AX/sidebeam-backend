package com.sidebar.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "gitlab")
public class GitLabProperties {

    private String apiUrl;
    private String accessToken;
    private String projectId; // Kept for backward compatibility
    private List<String> bookmarkProjects = new ArrayList<>();
    private String branch;
    private String bookmarkDataPath;
    private String fileExtension;
}
