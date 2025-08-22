# Sidebar Backend

Spring Boot 3 기반의 사이드바 백엔드 애플리케이션입니다.

## 📋 목차

- [시작하기](#시작하기)
- [설정값 관리(Jasypt/ENV)](#설정값-관리jasyptenv)
- [보안(API Key)](#보안api-key)
- [개발 가이드](#개발-가이드)
- [API 문서](#api-문서)
- [트러블슈팅](#트러블슈팅)

---

## 🚀 시작하기

### 사전 요구사항

- Java 21
- Gradle 8.x
- GitLab 개인 액세스 토큰 (read_repository 권한)

### 실행 방법

기본 실행:
```bash
./gradlew bootRun
```

로컬 프로파일로 실행(권장):
```bash
./gradlew bootRun -Dspring-boot.run.profiles=local
```

애플리케이션은 http://localhost:8095 에서 실행됩니다.

---

## 🔐 설정값 관리(Jasypt/ENV)

이 프로젝트는 민감한 설정값을 두 가지 방식으로 관리할 수 있습니다.

1) 로컬 개발환경: 환경변수 사용(권장)
- application-local.yml 에 매핑되어 있습니다.
- 설정 예시:
  ```bash
  export GITLAB_ACCESS_TOKEN='your-token'
  export GITLAB_ROOT_GROUP_ID='123456'
  export GITLAB_BRANCH='main'            # 선택
  export WEBHOOK_SECRET_TOKEN='your-webhook-secret'
  ```

2) ENC(...) 암호문 사용(Jasypt)
- application.yml 에 `ENC(...)` 형태로 값을 넣을 수 있습니다.
- 이 경우 애플리케이션 실행 전에 마스터 키를 설정하세요.
  ```bash
  export JASYPT_ENCRYPTOR_PASSWORD='your-master-key'
  ./gradlew bootRun
  ```
- 로컬에서도 ENC를 쓰고 싶다면 `-Dspring-boot.run.profiles=local` 과 함께 동일한 환경변수를 설정하세요.

참고: application.yml 주요 키
```yaml
server.port: 8095
springdoc.api-docs.path: /api-docs
springdoc.swagger-ui.path: /swagger-ui.html

gitlab:
  api-url: https://gitlab.com
  access-token: ENC(...) 또는 환경변수(GITLAB_ACCESS_TOKEN)
  root-group-id: ENC(...) 또는 환경변수(GITLAB_ROOT_GROUP_ID)
  branch: ${GITLAB_BRANCH:main}

webhook:
  secret-token: ENC(...) 또는 환경변수(WEBHOOK_SECRET_TOKEN)
```

주의: 이전 README에 언급된 Jasypt 전용 테스트 유틸리티(JasyptEncryptorTest, JasyptLocalProfileTest 등)는 현재 저장소에 존재하지 않습니다. 암호문 생성은 외부 Jasypt 도구/스크립트를 사용하거나 운영 표준에 따라 진행하세요.

---

## 🛡️ 보안(API Key)

운영(prod) 프로파일에서는 API Key 인증이 필수입니다. 다음 속성을 환경변수로 설정하세요.

- SECURITY_API_KEY_ENABLED=true
- SECURITY_API_KEY_VALUE=your-api-key-value
- SECURITY_API_KEY_HEADER=X-Api-Key            # 선택(기본값)
- SECURITY_API_KEY_EXCLUDES=/health,/actuator  # 선택, 콤마 구분

보안 가드: prod 프로파일에서 SecurityStartupValidator 가 기동 시 다음을 강제합니다.
- security.api-key.enabled 가 true 여야 합니다.
- security.api-key.value 가 비어있지 않아야 합니다.

미설정 시 애플리케이션은 시작에 실패합니다(의도된 동작).

---

## 🛠️ 개발 가이드

### 프로젝트 구조(요약)
```
src/
├── main/java/com/sidebeam/
│   ├── bookmark/                     # 북마크 관련 기능/초기화/검증
│   ├── common/security/config/       # SecurityStartupValidator 등 보안 설정
│   ├── external/gitlab/service/      # GitLabApiClient, RetryPolicy(SimpleRetryPolicy)
│   └── ...
├── main/resources/
│   ├── application.yml               # 기본 설정(ENC 사용 가능)
│   └── application-local.yml         # 로컬 전용(ENV 매핑)
└── test/                             # 테스트 코드
```

### GitLab 연동 개요
- WebClient 기반 GitLabApiClient 사용
- 간단한 재시도 정책(SimpleRetryPolicy) 지원: `gitlab.retry.max-attempts` 로 조정(기본 3)
- 필요 시 `gitlab.api.config` 로 엔드포인트 경로를 외부화

### HTTP/문서화
- SpringDoc OpenAPI 경로: /api-docs, Swagger UI: /swagger-ui.html
- 포트: 8095

---

## 📚 API 문서

- Swagger UI: http://localhost:8095/swagger-ui.html
- OpenAPI JSON: http://localhost:8095/api-docs

---

## 🔧 트러블슈팅

### 1) prod에서 애플리케이션이 즉시 종료됨
- 원인: API Key 미설정 또는 비활성화(SecurityStartupValidator)
- 조치:
  ```bash
  export SECURITY_API_KEY_ENABLED=true
  export SECURITY_API_KEY_VALUE='your-api-key'
  ```

### 2) ENC(...) 값이 복호화되지 않음
- 증상: `@Value` 주입 시 값이 그대로 `ENC(...)` 로 보임
- 원인: JASYPT_ENCRYPTOR_PASSWORD 미설정
- 조치:
  ```bash
  export JASYPT_ENCRYPTOR_PASSWORD='your-master-key'
  ./gradlew bootRun
  ```
  또는 로컬에서는 환경변수(평문) 사용으로 전환(application-local.yml 참고).

### 3) GitLab API 호출 실패
- 확인:
  1. GITLAB_ACCESS_TOKEN 권한(read_repository) 확인
  2. GITLAB_ROOT_GROUP_ID 값 확인
  3. gitlab.api-url 이 올바른지 확인(기본: https://gitlab.com)
  4. 일시적인 네트워크 오류가 잦다면 `gitlab.retry.max-attempts` 조정

### 4) Swagger 문서가 보이지 않음
- 경로 확인: /swagger-ui.html, /api-docs
- 프록시/보안 설정(API Key 헤더)로 인해 차단되지 않았는지 확인

---

## 📞 지원

문제가 지속되는 경우:
1. 애플리케이션 시작 로그 및 에러 스택 확인
2. 환경변수 설정 상태 확인(`echo $VARNAME`)
3. prod에서 API Key 관련 경고/오류 메시지(SecurityStartupValidator) 확인

---

## 🔒 보안 주의사항
- JASYPT_ENCRYPTOR_PASSWORD(사용 시)는 비밀로 안전하게 보관하세요.
- API Key 및 토큰은 절대 저장소에 커밋하지 마세요.
- 로그에 민감정보가 출력되지 않도록 주의하세요(필요 시 마스킹 적용).
- 로컬/운영 환경의 키/암호화 전략은 분리하여 관리하세요.