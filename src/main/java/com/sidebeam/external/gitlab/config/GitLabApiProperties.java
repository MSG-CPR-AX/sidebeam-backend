package com.sidebeam.external.gitlab.config;

import com.sidebeam.common.core.util.PropertyUtil;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

/**
 * GitLab API 엔드포인트 설정을 위한 구성 클래스입니다.
 * gitlab-api.yml 파일에서 설정을 로드합니다.
 * 
 * reference/rest 프로젝트의 PropertyUtil 방식을 참고하여 개선된 방식으로 구현했습니다.
 * DomainApiProperties와 유사한 패턴을 사용하여 PropertyUtil.getYmlProperties()로 YAML을 로드합니다.
 */
@Data
@Component
public class GitLabApiProperties implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(GitLabApiProperties.class);

    private static final String GITLAB_API_CONFIG_FILE = "classpath:gitlab/gitlab-api.yml";

    /**
     * YAML에서 로드된 설정을 저장하는 맵
     */
    private LinkedHashMap<String, Object> configMap;

    /**
     * 객체 초기화 후 호출되는 메서드입니다.
     * PropertyUtil을 사용하여 gitlab-api.yml 파일에서 설정을 로드합니다.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        loadFromYaml();
    }

    /**
     * PropertyUtil을 사용하여 gitlab-api.yml 파일에서 설정을 로드합니다.
     * reference/rest의 DomainApiProperties 방식을 참고했습니다.
     */
    private void loadFromYaml() {
        try {
            // PropertyUtil을 사용하여 YAML 파일을 LinkedHashMap으로 로드
            ResolvableType type = ResolvableType.forClassWithGenerics(LinkedHashMap.class, String.class, Object.class);
            this.configMap = PropertyUtil.getYmlProperties(GITLAB_API_CONFIG_FILE, type);

            log.info("GitLab API 속성이 {}에서 로드되었습니다", GITLAB_API_CONFIG_FILE);
        } catch (Exception e) {
            log.error("GitLab API 속성을 로드하는 중 오류 발생: {}", e.getMessage(), e);
            // 기본값으로 초기화
            this.configMap = new LinkedHashMap<>();
        }
    }

    private GroupEndpoints groups = new GroupEndpoints();
    private ProjectEndpoints projects = new ProjectEndpoints();
    private FileEndpoints files = new FileEndpoints();

    // Getter methods for backward compatibility
    public GroupEndpoints getGroupEndpoints() {
        populateEndpointsFromConfig();
        return groups;
    }

    public ProjectEndpoints getProjectEndpoints() {
        populateEndpointsFromConfig();
        return projects;
    }

    public FileEndpoints getFileEndpoints() {
        populateEndpointsFromConfig();
        return files;
    }

    /**
     * configMap에서 값을 추출하여 endpoint 객체들을 채웁니다.
     */
    private void populateEndpointsFromConfig() {
        if (configMap == null) {
            return;
        }

        // 그룹 설정 로드
        if (configMap.containsKey("groups")) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, String> groupsMap = (LinkedHashMap<String, String>) configMap.get("groups");
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
        if (configMap.containsKey("projects")) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> projectsMap = (LinkedHashMap<String, Object>) configMap.get("projects");
            if (projectsMap.containsKey("get")) {
                this.projects.setGet((String) projectsMap.get("get"));
            }
            if (projectsMap.containsKey("repository")) {
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> repoMap = (LinkedHashMap<String, Object>) projectsMap.get("repository");
                if (repoMap.containsKey("tree")) {
                    this.projects.getRepository().setTree((String) repoMap.get("tree"));
                }
                if (repoMap.containsKey("file")) {
                    @SuppressWarnings("unchecked")
                    LinkedHashMap<String, String> fileMap = (LinkedHashMap<String, String>) repoMap.get("file");
                    if (fileMap.containsKey("raw")) {
                        this.projects.getRepository().getFile().setRaw(fileMap.get("raw"));
                    }
                }
            }
        }

        // 파일 설정 로드
        if (configMap.containsKey("files")) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, String> filesMap = (LinkedHashMap<String, String>) configMap.get("files");
            if (filesMap.containsKey("get")) {
                this.files.setGet(filesMap.get("get"));
            }
        }
    }

    /**
     * GitLab 그룹 관련 API 엔드포인트 설정
     */
    @Data
    public static class GroupEndpoints {
        /**
         * 그룹 정보 조회 API 경로
         * GET /api/v4/groupEndPoints/{groupId}
         */
        private String get = "api/v4/groupEndPoints/{groupId}";

        /**
         * 하위 그룹 목록 조회 API 경로
         * GET /api/v4/groupEndPoints/{groupId}/subgroups
         */
        private String subgroups = "api/v4/groupEndPoints/{groupId}/subgroups";

        /**
         * 그룹 내 프로젝트 목록 조회 API 경로
         * GET /api/v4/groupEndPoints/{groupId}/projectEndPoints
         */
        private String projects = "api/v4/groupEndPoints/{groupId}/projectEndPoints";
    }

    /**
     * GitLab 프로젝트 관련 API 엔드포인트 설정
     */
    @Data
    public static class ProjectEndpoints {
        /**
         * 프로젝트 정보 조회 API 경로
         * GET /api/v4/projectEndPoints/{projectId}
         */
        private String get = "api/v4/projectEndPoints/{projectId}";

        /**
         * 저장소 파일 트리 조회 API 경로
         * GET /api/v4/projectEndPoints/{projectId}/repository/tree
         */
        private Repository repository = new Repository();

        @Data
        public static class Repository {
            /**
             * 저장소 파일 트리 조회 API 경로
             * GET /api/v4/projectEndPoints/{projectId}/repository/tree
             */
            private String tree = "api/v4/projectEndPoints/{projectId}/repository/tree";

            /**
             * 파일 내용 조회 API 경로
             * GET /api/v4/projectEndPoints/{projectId}/repository/fileEndpoints/{filePath}/raw
             */
            private File file = new File();

            @Data
            public static class File {
                /**
                 * 파일 내용 조회 API 경로
                 * GET /api/v4/projectEndPoints/{projectId}/repository/fileEndpoints/{filePath}/raw
                 */
                private String raw = "api/v4/projectEndPoints/{projectId}/repository/fileEndpoints/{filePath}/raw";
            }
        }
    }

    /**
     * GitLab 파일 관련 API 엔드포인트 설정
     */
    @Data
    public static class FileEndpoints {
        /**
         * 파일 정보 조회 API 경로
         * GET /api/v4/projectEndPoints/{projectId}/repository/fileEndpoints/{filePath}
         */
        private String get = "api/v4/projectEndPoints/{projectId}/repository/fileEndpoints/{filePath}";
    }
}
