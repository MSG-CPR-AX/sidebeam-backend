# Sidebar Backend

[![Java](https://img.shields.io/badge/Java-24-blue.svg)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-9.0.0-yellow.svg)](https://gradle.org/)

`sidebar-backend`는 GitLab 리포지토리에서 북마크 데이터를 가져와 API 형태로 제공하는 고성능 백엔드 애플리케이션입니다.

## 1. 🎯 핵심 아키텍처 철학

이 프로젝트는 최신 Java의 기능을 적극적으로 활용하여 **높은 개발 생산성**과 **최상의 리소스 효율성**을 동시에 달성하는 것을 목표로 합니다.

### Imperative Code, Reactive Performance
전통적인 Spring MVC의 '요청 당 스레드' 모델은 코드가 단순하고 직관적이라는 큰 장점이 있지만, 스레드 리소스 한계로 인해 동시성 처리에 약점을 보였습니다. 이를 해결하기 위해 등장한 리액티브 프로그래밍(WebFlux)은 뛰어난 리소스 효율성을 제공하지만, 코드의 복잡성과 디버깅의 어려움이라는 큰 비용을 요구합니다.

**이 프로젝트는 Java 24의 가상 스레드(Virtual Threads)를 통해 이 두 세계의 장점만을 취합니다.**

개발자는 복잡한 `Mono`, `Flux` 없이 익숙하고 단순한 동기/블로킹 스타일로 코드를 작성합니다. 내부적으로는 JVM이 I/O 대기 시점에 OS 스레드를 회수하여 다른 작업에 할당하므로, 리액티브 모델과 동일한 수준의 높은 처리량과 확장성을 확보할 수 있습니다.

### I/O 분리를 통한 효율성 극대화
- **Inbound (외부 요청 처리):** Spring MVC를 사용하여 들어오는 HTTP 요청을 처리합니다. 가상 스레드 위에서 동작하므로, 코드는 단순함을 유지하면서도 높은 동시성을 감당할 수 있습니다.
- **Outbound (외부 서비스 호출):** `WebClient` (WebFlux)를 사용하여 외부 GitLab API를 호출합니다. Non-blocking I/O 클라이언트를 사용하면 가상 스레드가 I/O를 기다리는 동안 OS 스레드를 점유하지 않아, 시스템 전체의 리소스 효율성이 극대화됩니다.

이 아키텍처는 현대 Java 애플리케이션의 모범 사례(Best Practice)로, 유지보수가 용이한 코드로 최고의 성능을 이끌어내는 것을 지향합니다.

## 2. 🛠️ 주요 기술 스택 및 선택 이유

| 기술 분류 | 기술 이름 | 버전 | 선택 이유 |
|---|---|---|---|
| **Core** | Java | 24 | **가상 스레드(Virtual Threads)**를 활용하여 높은 동시성 처리 |
| | Spring Boot | 3.5.4 | 최신 기능 지원, 자동 설정, 강력한 의존성 관리 |
| | Gradle | 9.0.0 | 유연하고 빠른 빌드 시스템 |
| **Web** | Spring MVC | - | 가상 스레드와 결합하여 생산성과 성능을 모두 확보 |
| | Spring WebFlux (WebClient) | - | Non-blocking I/O 기반 외부 API 호출 |
| **Data & Cache** | Caffeine | - | 높은 성능의 인메모리 캐시를 통해 반복적인 API 호출 최소화 |
| **Security** | Spring Security | - | API Key 인증 등 표준적이고 강력한 보안 기능 제공 |
| | Jasypt | 3.0.5 | 설정 파일 내 민감 정보(토큰 등)를 간편하게 암호화 |
| **Resilience** | Resilience4j | 2.3.0 | 외부 API 연동 시 재시도(Retry), 서킷 브레이커 등을 통한 안정성 확보 |
| **Testing** | JUnit 5 | 5.13.4 | 표준 단위/통합 테스트 프레임워크 |
| | ArchUnit | 1.4.1 | 아키텍처 규칙을 코드로 강제하여 시스템의 구조적 무결성 유지 |
| | Jacoco | - | 테스트 커버리지 측정 및 품질 관리 |
| **Docs** | SpringDoc OpenAPI | 2.8.9 | Swagger UI를 통한 자동 API 문서화 |
| **Ops** | Logstash Logback Encoder | 8.0 | 구조화된 JSON 로깅을 통해 중앙 로그 시스템과의 연동성 강화 |
| | Spring Boot Actuator | - | `/health`, `/metrics` 등 운영에 필수적인 엔드포인트 제공 |

## 3. 🚀 시작하기

### 사전 요구사항
- Java 24
- Gradle 9.x
- GitLab 개인 액세스 토큰 (`read_repository` 권한)

### 실행 방법

1.  **리포지토리 클론:**
    ```bash
    git clone [repository_url]
    cd sidebar-backend
    ```

2.  **환경 변수 설정 (로컬 개발용):**
    `application-local.yml` 프로파일은 환경 변수를 통해 주요 설정을 주입받습니다. 아래와 같이 설정해주세요.
    ```bash
    export GITLAB_ACCESS_TOKEN='your-gitlab-personal-access-token'
    export GITLAB_ROOT_GROUP_ID='123456' # 데이터를 가져올 최상위 그룹 ID
    export WEBHOOK_SECRET_TOKEN='your-webhook-secret-token'
    # export JASYPT_ENCRYPTOR_PASSWORD='your-master-key' # Jasypt 암호문 사용 시
    ```

3.  **애플리케이션 실행:**
    가상 스레드를 활성화하여 실행하는 것을 권장합니다.

    - **기본 실행 (로컬 프로파일):**
      ```bash
      ./gradlew bootRun -Dspring-boot.run.profiles=local
      ```
    - **가상 스레드 활성화 실행 (권장):**
      `src/main/resources/application.yml`에 다음 설정을 추가한 후 위와 동일하게 실행합니다.
      ```yaml
      spring:
        threads:
          virtual:
            enabled: true
      ```

4.  **애플리케이션 확인:**
    - API 서버: `http://localhost:8095`
    - API 문서 (Swagger UI): `http://localhost:8095/swagger-ui.html`

## 4. ⚙️ 설정 관리

이 프로젝트는 `Jasypt`를 통해 민감 정보를 암호화하고, Spring 프로파일을 통해 환경별 설정을 분리합니다.

- **`application.yml`**: 모든 환경의 공통 설정 및 운영 환경 기본값. `ENC(...)` 형태의 암호화된 값이 포함될 수 있습니다.
- **`application-local.yml`**: 로컬 개발 환경 전용 설정. 환경 변수로부터 값을 주입받도록 설계되었습니다.
- **`application-test.yml`**: 테스트 환경 전용 설정.

`JASYPT_ENCRYPTOR_PASSWORD` 환경 변수가 설정되어 있으면 애플리케이션 실행 시 `ENC(...)` 값을 자동으로 복호화합니다.

## 5. 🛡️ 보안

운영(`prod`) 프로파일에서는 API Key 인증이 기본적으로 활성화되도록 권장됩니다. `SecurityStartupValidator`는 관련 설정이 누락될 경우 애플리케이션 시작을 차단하여 안전한 배포를 강제합니다.

- **필수 환경 변수 (운영 시):**
  - `SECURITY_API_KEY_ENABLED=true`
  - `SECURITY_API_KEY_VALUE=your-secure-api-key`
- **API Key 전달:** `X-Api-Key` HTTP 헤더를 통해 전달합니다.

## 6. 🧪 테스트 전략

본 프로젝트는 높은 코드 품질과 안정성을 유지하기 위해 다층적 테스트 전략을 사용합니다.

- **단위 테스트:** 각 클래스 및 메서드의 논리를 검증합니다.
- **통합 테스트:** 주요 컴포넌트(DB, 외부 API Mock)를 통합하여 검증합니다.
- **Web MVC 테스트:** 컨트롤러 계층의 동작을 검증합니다.
- **아키텍처 테스트 (ArchUnit):** '서비스는 컨트롤러를 호출할 수 없다'와 같은 아키텍처 규칙을 코드로 검증하여 아키텍처가 무너지는 것을 방지합니다.

모든 테스트는 `./gradlew test` 명령으로 실행할 수 있으며, `./gradlew build` 실행 시 자동으로 수행됩니다.

## 7. 📦 컨테이너화

프로젝트 루트에 `Dockerfile`이 포함되어 있습니다. 이 파일은 멀티스테이지 빌드(multi-stage build)를 사용하여 최종 이미지 크기를 최적화하고 보안을 강화합니다.

- **빌드:** `sudo docker build -t sidebar-backend .`
- **실행:** `sudo docker run -p 8095:8095 sidebar-backend`

## 8. 🔍 로깅 및 모니터링

- **구조화된 로깅:** Logstash Logback Encoder를 사용하여 모든 로그를 JSON 형식으로 출력합니다. 이는 ELK, Datadog 등 중앙화된 로깅 시스템과의 손쉬운 연동을 위함입니다.
- **모니터링:** Spring Boot Actuator를 통해 `/actuator/health`, `/actuator/metrics`, `/actuator/prometheus` 등 다양한 운영 엔드포인트를 제공합니다. 이를 통해 서비스의 상태를 실시간으로 모니터링할 수 있습니다.