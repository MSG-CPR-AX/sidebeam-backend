# Security Guide

이 문서는 `sidebar-backend` 애플리케이션의 주요 보안 기능과 정책에 대해 설명합니다. 안정적인 서비스 운영을 위해 반드시 숙지해야 합니다.

## 1. 민감 정보 암호화 (Jasypt)

애플리케이션 설정(`application.yml`)에 포함된 API 토큰, 비밀 키 등 민감한 정보는 **반드시 암호화하여 저장**해야 합니다. 이 프로젝트는 `Jasypt` 라이브러리를 통해 이 과정을 간소화합니다.

### 동작 원리
- `Jasypt`는 마스터 키(Master Key)를 사용하여 설정 파일 내의 특정 문자열을 암호화하거나 복호화합니다.
- 암호화된 문자열은 `ENC(...)` 형태로 감싸져 있으며, 애플리케이션 실행 시 마스터 키를 통해 원본 값으로 복호화되어 메모리에 로드됩니다.

### 설정 방법

1.  **마스터 키 설정:**
    애플리케이션을 실행하는 환경에 **환경 변수**로 마스터 키를 주입해야 합니다.
    ```bash
    export JASYPT_ENCRYPTOR_PASSWORD='your-strong-and-secret-master-key'
    ```
    이 키는 절대로 소스 코드에 하드코딩하거나 버전 관리에 포함해서는 안 됩니다.

2.  **암호문 생성:**
    새로운 민감 정보를 추가할 때는 Jasypt가 제공하는 외부 도구나 스크립트를 사용하여 암호화된 `ENC(...)` 문자열을 생성해야 합니다.
    > (주의: 프로젝트 내에 암호화 유틸리티는 포함되어 있지 않으므로, 표준 운영 절차에 따라 안전하게 암호문을 생성하세요.)

3.  **`application.yml`에 적용:**
    생성된 암호문을 `application.yml` 파일에 적용합니다.
    ```yaml
    gitlab:
      access-token: ENC(cIe5EbJKeVmkxdEKczOsGtzePJ0Vx8pz+AlGnCC4l+Pf8apzW1mLkI4hpdQIOYtO...)
    ```

`JASYPT_ENCRYPTOR_PASSWORD` 환경 변수 없이 애플리케이션을 실행하면, 암호문이 복호화되지 않아 `ENC(...)`가 그대로 값으로 주입되므로 서비스가 정상 동작하지 않습니다.

## 2. API Key 인증

운영 환경(`prod` 프로파일)에서는 허가되지 않은 접근을 막기 위해 **API Key 기반 인증을 강제**합니다. 로컬 또는 테스트 환경에서는 편의를 위해 이 기능이 비활성화될 수 있습니다.

### 동작 원리
- `ApiKeyAuthFilter`가 모든 들어오는 요청의 특정 HTTP 헤더(`X-Api-Key`)를 확인합니다.
- 헤더에 포함된 API Key 값이 서버에 설정된 값과 일치하는지 검증합니다.
- Key가 유효하지 않거나 존재하지 않으면, 요청은 `401 Unauthorized` 에러와 함께 거부됩니다.
- `/actuator/health`와 같은 일부 필수 경로는 인증 검사에서 제외될 수 있습니다.

### 설정 방법 (운영 환경)

운영 환경에서는 다음 환경 변수를 **반드시** 설정해야 합니다.

-   **`SECURITY_API_KEY_ENABLED=true`**: API Key 인증 기능을 활성화합니다.
-   **`SECURITY_API_KEY_VALUE='your-very-secure-and-random-api-key'`**: 클라이언트와 서버가 공유할 실제 API Key 값입니다.
-   **`SECURITY_API_KEY_HEADER=X-Api-Key`**: (선택 사항) API Key를 전달할 헤더 이름입니다. 기본값은 `X-Api-Key`입니다.
-   **`SECURITY_API_KEY_EXCLUDES=/swagger-ui.html,/api-docs`**: (선택 사항) 인증을 제외할 경로를 콤마(`,`)로 구분하여 추가합니다.

### 안전 장치 (Startup Validator)
`SecurityStartupValidator`는 운영 프로파일로 애플리케이션이 시작될 때, 위의 `enabled`와 `value` 설정이 올바르게 구성되었는지 검사합니다. 만약 하나라도 누락되면, 안전을 위해 **애플리케이션 시작을 즉시 실패시킵니다.** 이는 설정 실수로 인해 보안이 비활성화된 채 서비스가 배포되는 것을 원천적으로 방지합니다.

## 3. 보안 관련 책임
- **마스터 키 및 API Key 관리:** 이 키들은 매우 민감한 정보이므로, Vault, AWS Secrets Manager 등 안전한 방법으로 저장하고 관리해야 합니다.
- **민감 정보 노출 금지:** 로그나 에러 메시지에 민감한 정보(토큰, 키 등)가 노출되지 않도록 주의해야 합니다.
- **의존성 관리:** 정기적으로 라이브러리 의존성을 스캔하여 알려진 보안 취약점을 패치해야 합니다.