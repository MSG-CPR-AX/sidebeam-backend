# Error Response Guide

이 문서는 `sidebar-backend` API의 표준 에러 응답 구조와 주요 에러 코드에 대해 설명합니다. 모든 에러는 일관된 JSON 형식으로 반환됩니다.

## 1. 에러 응답 구조

API 호출이 실패할 경우, `success` 필드는 `false`가 되며 `error` 필드에 상세한 오류 정보가 포함됩니다.

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "SPECIFIC_ERROR_CODE",
    "message": "A human-readable description of the error.",
    "details": {
      "path": "/requested/path",
      "correlationId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
      "hint": "Optional hint about the error.",
      "fieldErrors": [
        {
          "field": "fieldName",
          "reason": "Description of why the field is invalid."
        }
      ]
    }
  }
}
```

-   **`code`**: 에러의 종류를 나타내는 고유한 문자열 코드입니다. (아래 표 참고)
-   **`message`**: 사람이 읽을 수 있는 형태의 에러 설명입니다.
-   **`details`**: 에러에 대한 추가적인 구조화된 정보입니다.
    -   `path`: 요청이 실패한 경로
    -   `correlationId`: 요청 추적을 위한 고유 ID
    -   `hint`: 에러 해결에 도움이 될 수 있는 힌트 (선택 사항)
    -   `fieldErrors`: 입력값 검증 실패 시, 어떤 필드가 어떤 이유로 실패했는지에 대한 목록 (선택 사항)

## 2. 주요 에러 코드

다음은 `ErrorCode` Enum에 정의된 주요 에러 코드 목록입니다.

| Error Code | HTTP Status | 설명 |
| :--- | :--- | :--- |
| **일반 오류** | | |
| `INTERNAL_SERVER_ERROR` | 500 | 예측하지 못한 내부 서버 오류가 발생했습니다. |
| `INVALID_REQUEST` | 400 | 요청의 형식이 잘못되었거나 필수 값이 누락되었습니다. |
| `RESOURCE_NOT_FOUND` | 404 | 요청한 리소스를 찾을 수 없습니다. |
| `SERVICE_UNAVAILABLE` | 503 | 현재 서비스를 사용할 수 없습니다. (예: 점검 중) |
| **입력값 검증** | | |
| `VALIDATION_ERROR` | 400 | 하나 이상의 필드에서 유효성 검사에 실패했습니다. (`details.fieldErrors` 참고) |
| `MISSING_REQUIRED_PARAMETER` | 400 | 필수 요청 파라미터가 누락되었습니다. |
| `INVALID_PARAMETER_FORMAT` | 400 | 파라미터의 형식이 올바르지 않습니다. (예: 숫자 필드에 문자열) |
| `PARAMETER_OUT_OF_RANGE` | 400 | 파라미터 값이 허용된 범위를 벗어났습니다. |
| **북마크 관련** | | |
| `BOOKMARK_NOT_FOUND` | 404 | 특정 북마크를 찾을 수 없습니다. |
| `BOOKMARK_PARSING_ERROR` | 500 | 북마크 원본 데이터(YAML 등)를 파싱하는 데 실패했습니다. |
| `BOOKMARK_VALIDATION_ERROR`| 400 | 북마크 데이터의 내용이 비즈니스 규칙에 맞지 않습니다. |
| **외부 서비스 (GitLab)** | | |
| `GITLAB_CONNECTION_ERROR` | 503 | GitLab 서버에 연결할 수 없습니다. (네트워크 문제 또는 GitLab 다운) |
| `GITLAB_API_ERROR` | 502 | GitLab API가 에러를 반환했습니다. |
| `GITLAB_FILE_NOT_FOUND` | 404 | GitLab 리포지토리에서 요청한 파일을 찾을 수 없습니다. |
| `EXTERNAL_SERVICE_ERROR` | 502 | GitLab 이외의 다른 외부 서비스 호출에 실패했습니다. |
| **인증 및 권한** | | |
| `UNAUTHORIZED` | 401 | 유효한 API Key가 없거나 잘못되어 요청이 거부되었습니다. |
| `FORBIDDEN` | 403 | 인증은 되었으나 해당 리소스에 접근할 권한이 없습니다. |
| **비즈니스 로직** | | |
| `BUSINESS_RULE_VIOLATION` | 400 | 비즈니스 규칙을 위반하여 요청을 처리할 수 없습니다. |
| `DUPLICATE_RESOURCE` | 409 | 생성하려는 리소스가 이미 존재합니다. (예: 동일한 ID의 북마크) |
| `RESOURCE_STATE_INVALID` | 400 | 현재 리소스의 상태에서는 요청된 작업을 수행할 수 없습니다. |
| **기타 기술 오류** | | |
| `CACHE_ERROR` | 500 | 캐시를 읽거나 쓰는 과정에서 오류가 발생했습니다. |
| `SCHEMA_VALIDATION_ERROR`| 422 | 데이터가 정의된 JSON 스키마와 일치하지 않습니다. |