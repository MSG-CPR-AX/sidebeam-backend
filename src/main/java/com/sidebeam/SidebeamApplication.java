package com.sidebeam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import java.lang.String;

/**
 * Main application class for the Sidebar Backend service.
 * This service provides bookmark data from GitLab repositories.
 */
@SpringBootApplication
@EnableCaching
@EnableScheduling
@OpenAPIDefinition(
    info = @Info(
        title = " Backend API",
        version = "1.0",
        description = "REST API for accessing bookmark data from GitLab repositories"
    )
)
public class SidebeamApplication {

    public static void main(String[] args) {
        SpringApplication.run(SidebeamApplication.class, args);
    }
}
