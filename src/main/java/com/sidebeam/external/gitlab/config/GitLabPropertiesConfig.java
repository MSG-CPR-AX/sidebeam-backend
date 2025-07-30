package com.sidebeam.external.gitlab.config;

import com.sidebeam.common.core.util.PropertyUtil;
import com.sidebeam.external.gitlab.config.property.GitLabApiProperties;
import com.sidebeam.external.gitlab.config.property.GitLabProperties;
import com.sidebeam.external.gitlab.config.property.WebhookProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * GitLab 관련 설정을 관리하는 구성 클래스입니다.
 * PropertyUtil.getYmlProperties를 활용하여 Properties 클래스들을 초기화합니다.
 * 
 * 이 클래스는 PropertyUtil을 사용하여 YAML 파일에서 설정을 로드하고
 * 해당 설정으로 Properties 객체들을 초기화하는 방법을 보여줍니다.
 */
@Slf4j
@Configuration
public class GitLabPropertiesConfig {

    /**
     * PropertyUtil을 사용하여 GitLab API 속성을 초기화합니다.
     * 'gitlab.api.config' 속성에 지정된 경로의 YAML 파일에서 설정을 로드합니다.
     *
     * @param configPath 'gitlab.api.config' 속성에서 주입된 설정 파일 경로
     * @return GitLabApiProperties 인스턴스
     */
    @Bean
    public GitLabApiProperties gitLabApiPropertiesFromYml(@Value("${gitlab.api.config}") String configPath) {
        log.info("PropertyUtil을 사용하여 GitLab API 속성을 초기화합니다.");
        
        try {
            // PropertyUtil.getYmlProperties를 사용하여 gitlab-api.yml 파일에서 설정 로드
            log.info("로딩할 GitLab API 설정 파일 경로: {}", configPath);
            GitLabApiProperties properties = PropertyUtil.getYmlProperties(configPath, GitLabApiProperties.class);
            
            log.info("GitLab API 속성이 PropertyUtil을 통해 성공적으로 로드되었습니다.");
            return properties;
            
        } catch (Exception e) {
            log.error("PropertyUtil을 사용한 GitLab API 속성 로드 중 오류 발생: {}", e.getMessage(), e);
            
            // 오류 발생 시 기본값으로 초기화된 객체 반환
            GitLabApiProperties defaultProperties = new GitLabApiProperties();
            log.warn("기본값으로 GitLab API 속성을 초기화했습니다.");
            return defaultProperties;
        }
    }

    /**
     * PropertyUtil을 사용하여 GitLab 속성을 초기화합니다.
     * application.yml의 gitlab 섹션에서 설정을 로드합니다.
     * 
     * @return GitLabProperties 인스턴스
     */
    @Bean
    public GitLabProperties gitLabPropertiesFromYml() {
        log.info("PropertyUtil을 사용하여 GitLab 속성을 초기화합니다.");
        
        try {
            // PropertyUtil.getYmlProperties를 사용하여 application.yml에서 gitlab 섹션 로드
            String configPath = "classpath:application.yml";
            GitLabProperties properties = PropertyUtil.getYmlProperties(configPath, "gitlab", GitLabProperties.class);
            
            log.info("GitLab 속성이 PropertyUtil을 통해 성공적으로 로드되었습니다.");
            return properties;
            
        } catch (Exception e) {
            log.error("PropertyUtil을 사용한 GitLab 속성 로드 중 오류 발생: {}", e.getMessage(), e);
            
            // 오류 발생 시 기본값으로 초기화된 객체 반환
            GitLabProperties defaultProperties = new GitLabProperties();
            log.warn("기본값으로 GitLab 속성을 초기화했습니다.");
            return defaultProperties;
        }
    }

    /**
     * PropertyUtil을 사용하여 Webhook 속성을 초기화합니다.
     * application.yml의 webhook 섹션에서 설정을 로드합니다.
     * 
     * @return WebhookProperties 인스턴스
     */
    @Bean
    public WebhookProperties webhookPropertiesFromYml() {
        log.info("PropertyUtil을 사용하여 Webhook 속성을 초기화합니다.");
        
        try {
            // PropertyUtil.getYmlProperties를 사용하여 application.yml에서 webhook 섹션 로드
            String configPath = "classpath:application.yml";
            WebhookProperties properties = PropertyUtil.getYmlProperties(configPath, "webhook", WebhookProperties.class);
            
            log.info("Webhook 속성이 PropertyUtil을 통해 성공적으로 로드되었습니다.");
            return properties;
            
        } catch (Exception e) {
            log.error("PropertyUtil을 사용한 Webhook 속성 로드 중 오류 발생: {}", e.getMessage(), e);
            
            // 오류 발생 시 기본값으로 초기화된 객체 반환
            WebhookProperties defaultProperties = new WebhookProperties();
            log.warn("기본값으로 Webhook 속성을 초기화했습니다.");
            return defaultProperties;
        }
    }
}