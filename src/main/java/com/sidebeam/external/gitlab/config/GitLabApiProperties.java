package com.sidebeam.external.gitlab.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * GitLab API 엔드포인트 설정을 위한 구성 클래스입니다.
 * gitlab-api.yml 파일에서 설정을 로드합니다.
 */
@Data
@Component
public class GitLabApiProperties implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(GitLabApiProperties.class);

    private static final String GITLAB_API_CONFIG_FILE = "gitlab/gitlab-api.yml";

    /**
     * 객체 초기화 후 호출되는 메서드입니다.
     * gitlab-api.yml 파일에서 설정을 로드합니다.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        loadFromYaml();
    }

    /**
     * gitlab-api.yml 파일에서 설정을 로드합니다.
     */
    private void loadFromYaml() {
        try {
            ClassPathResource resource = new ClassPathResource(GITLAB_API_CONFIG_FILE);
            try (InputStream inputStream = resource.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

                // YAML을 Map으로 읽어서 처리
                Map<String, Object> yamlMap = mapper.readValue(inputStream, new TypeReference<Map<String, Object>>() {});

                // 그룹 설정 로드
                if (yamlMap.containsKey("groups")) {
                    Map<String, String> groupsMap = (Map<String, String>) yamlMap.get("groups");
                    if (groupsMap.containsKey("get")) {
                        this.groups.setGet(groupsMap.get("get"));
                    }
                    if (groupsMap.containsKey("subgroups")) {
                        this.groups.setSubgroups(groupsMap.get("subgroups"));
                    }
                    if (groupsMap.containsKey("projects")) {
                        this.groups.setProjects(groupsMap.get("projects"));
                    }
                }

                // 프로젝트 설정 로드
                if (yamlMap.containsKey("projects")) {
                    Map<String, Object> projectsMap = (Map<String, Object>) yamlMap.get("projects");
                    if (projectsMap.containsKey("get")) {
                        this.projects.setGet((String) projectsMap.get("get"));
                    }
                    if (projectsMap.containsKey("repository")) {
                        Map<String, Object> repoMap = (Map<String, Object>) projectsMap.get("repository");
                        if (repoMap.containsKey("tree")) {
                            this.projects.getRepository().setTree((String) repoMap.get("tree"));
                        }
                        if (repoMap.containsKey("file")) {
                            Map<String, String> fileMap = (Map<String, String>) repoMap.get("file");
                            if (fileMap.containsKey("raw")) {
                                this.projects.getRepository().getFile().setRaw(fileMap.get("raw"));
                            }
                        }
                    }
                }

                // 파일 설정 로드
                if (yamlMap.containsKey("files")) {
                    Map<String, String> filesMap = (Map<String, String>) yamlMap.get("files");
                    if (filesMap.containsKey("get")) {
                        this.files.setGet(filesMap.get("get"));
                    }
                }

                log.info("GitLab API 속성이 {}에서 로드되었습니다", GITLAB_API_CONFIG_FILE);
            }
        } catch (IOException e) {
            log.error("GitLab API 속성을 로드하는 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private Groups groups = new Groups();
    private Projects projects = new Projects();
    private Files files = new Files();

    /**
     * GitLab 그룹 관련 API 엔드포인트 설정
     */
    @Data
    public static class Groups {
        /**
         * 그룹 정보 조회 API 경로
         * GET /api/v4/groups/{groupId}
         */
        private String get = "api/v4/groups/{groupId}";

        /**
         * 하위 그룹 목록 조회 API 경로
         * GET /api/v4/groups/{groupId}/subgroups
         */
        private String subgroups = "api/v4/groups/{groupId}/subgroups";

        /**
         * 그룹 내 프로젝트 목록 조회 API 경로
         * GET /api/v4/groups/{groupId}/projects
         */
        private String projects = "api/v4/groups/{groupId}/projects";
    }

    /**
     * GitLab 프로젝트 관련 API 엔드포인트 설정
     */
    @Data
    public static class Projects {
        /**
         * 프로젝트 정보 조회 API 경로
         * GET /api/v4/projects/{projectId}
         */
        private String get = "api/v4/projects/{projectId}";

        /**
         * 저장소 파일 트리 조회 API 경로
         * GET /api/v4/projects/{projectId}/repository/tree
         */
        private Repository repository = new Repository();

        @Data
        public static class Repository {
            /**
             * 저장소 파일 트리 조회 API 경로
             * GET /api/v4/projects/{projectId}/repository/tree
             */
            private String tree = "api/v4/projects/{projectId}/repository/tree";

            /**
             * 파일 내용 조회 API 경로
             * GET /api/v4/projects/{projectId}/repository/files/{filePath}/raw
             */
            private File file = new File();

            @Data
            public static class File {
                /**
                 * 파일 내용 조회 API 경로
                 * GET /api/v4/projects/{projectId}/repository/files/{filePath}/raw
                 */
                private String raw = "api/v4/projects/{projectId}/repository/files/{filePath}/raw";
            }
        }
    }

    /**
     * GitLab 파일 관련 API 엔드포인트 설정
     */
    @Data
    public static class Files {
        /**
         * 파일 정보 조회 API 경로
         * GET /api/v4/projects/{projectId}/repository/files/{filePath}
         */
        private String get = "api/v4/projects/{projectId}/repository/files/{filePath}";
    }
}
