# Operations Guide

이 문서는 `sidebar-backend` 애플리케이션의 배포, 모니터링, 로깅 등 운영에 필요한 절차와 가이드를 제공합니다.

## 1. 배포 (Deployment)

이 애플리케이션은 `Dockerfile`을 통해 컨테이너 환경에 배포하는 것을 표준으로 합니다.

### 사전 요구사항
- Docker 또는 호환 가능한 컨테이너 런타임
- 애플리케이션 실행에 필요한 환경 변수 (보안 및 설정 가이드 참고)

### 빌드 및 실행 절차

1.  **Docker 이미지 빌드:**
    프로젝트 루트 디렉터리에서 아래 명령어를 실행하여 Docker 이미지를 빌드합니다.
    ```bash
    sudo docker build -t sidebar-backend:latest .
    ```
    이 과정은 멀티스테이지 빌드를 통해 소스 코드를 컴파일하고, 최종적으로 경량화된 JRE 이미지 위에 실행 가능한 JAR 파일만 포함된 이미지를 생성합니다.

2.  **환경 변수 준비:**
    운영에 필요한 모든 환경 변수(Jasypt 마스터 키, API Key, GitLab 토큰 등)를 파일(`prod.env`)로 준비하거나, Kubernetes Secrets/ConfigMap, Docker Compose 등 해당 환경에 맞는 방식으로 준비합니다.
    ```
    # 예시: prod.env 파일
    JASYPT_ENCRYPTOR_PASSWORD=your-strong-and-secret-master-key
    SECURITY_API_KEY_ENABLED=true
    SECURITY_API_KEY_VALUE=your-very-secure-and-random-api-key
    GITLAB_ACCESS_TOKEN=your-gitlab-personal-access-token
    GITLAB_ROOT_GROUP_ID=123456
    WEBHOOK_SECRET_TOKEN=your-webhook-secret-token
    ```

3.  **Docker 컨테이너 실행:**
    준비된 환경 변수와 함께 Docker 컨테이너를 실행합니다.
    ```bash
    sudo docker run --name sidebar-app -d \
      -p 8095:8095 \
      --env-file prod.env \
      sidebar-backend:latest
    ```
    - `-d`: 백그라운드 실행
    - `-p 8095:8095`: 호스트의 8095 포트를 컨테이너의 8095 포트로 매핑
    - `--env-file`: 환경 변수 파일을 컨테이너에 전달

## 2. 모니터링 (Monitoring)

애플리케이션의 상태는 Spring Boot Actuator를 통해 노출되는 엔드포인트를 통해 확인할 수 있습니다.

### 주요 엔드포인트
-   **Health Check:** `http://localhost:8095/actuator/health`
    - 애플리케이션의 전반적인 상태, DB 연결, 디스크 공간 등을 종합하여 상태를 반환합니다. Kubernetes의 Liveness/Readiness Probe에 활용될 수 있습니다.
-   **Metrics:** `http://localhost:8095/actuator/metrics`
    - JVM 성능(메모리, 스레드), HTTP 요청 수, 응답 시간 등 다양한 메트릭 정보를 제공합니다.
-   **Prometheus:** `http://localhost:8095/actuator/prometheus`
    - Prometheus가 수집할 수 있는 형식으로 메트릭을 노출합니다. 이 엔드포인트를 Prometheus 타겟으로 등록하여 시계열 데이터를 수집하고 Grafana 등으로 시각화할 수 있습니다.

### Caffeine 캐시 모니터링
`CacheConfig`에 캐시 통계 기록(`recordStats()`)이 활성화되어 있으므로, Actuator 메트릭스를 통해 캐시의 적중률(hit rate), 부재율(miss rate), 크기(size) 등을 모니터링할 수 있습니다.
- 예시 메트릭: `cache.gets`, `cache.puts`, `cache.size` 등

## 3. 로깅 (Logging)

이 애플리케이션은 `Logstash Logback Encoder`를 사용하여 **구조화된 JSON 형식**으로 로그를 출력합니다.

### 로그 형식
모든 로그는 일반 텍스트가 아닌, 다음과 같은 JSON 객체 형태를 가집니다.
```json
{
  "@timestamp": "2025-08-24T18:00:00.123Z",
  "message": "REST request to get all bookmarks",
  "logger_name": "com.sidebeam.bookmark.controller.BookmarkController",
  "thread_name": "virtual-thread-1",
  "level": "DEBUG",
  "level_value": 10000,
  "correlation_id": "a1b2c3d4-e5f6-7890-1234-567890abcdef"
}
```
- **장점:** 이 형식은 Filebeat, Fluentd 등 로그 수집기가 파싱하기 매우 용이하며, Elasticsearch, Splunk, Datadog과 같은 중앙 로깅 시스템에서 로그를 검색, 필터링, 분석하기에 최적화되어 있습니다.
- **`correlation_id`**: 분산 환경에서 특정 요청의 전체 흐름을 추적하는 데 사용되는 고유 ID입니다.

### 로그 확인
컨테이너 환경에서는 `docker logs` 명령을 통해 이 JSON 로그를 직접 확인할 수 있습니다.
```bash
sudo docker logs sidebar-app
```
운영 환경에서는 로그 수집 에이전트를 통해 이 로그들을 중앙 로깅 시스템으로 전송하는 것을 권장합니다.