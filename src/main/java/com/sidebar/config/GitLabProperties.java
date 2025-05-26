package com.sidebar.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "gitlab")
public class GitLabProperties {

    private String apiUrl;
    private String accessToken;
    private String projectId;
    private String branch;
    private String bookmarkDataPath;
    private String fileExtension;
}