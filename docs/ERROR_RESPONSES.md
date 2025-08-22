# 오류 응답 스키마 문서

## 개요
이 문서는 Sidebar Backend API 전반에서 사용되는 표준화된 오류 응답 형식을 설명하며, 오류 코드, 응답 구조 및 예제를 포함합니다.

## 표준 응답 형식

### 성공 응답
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2024-08-22T13:00:00Z"
}
```

### 오류 응답
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {}
  },
  "timestamp": "2024-08-22T13:00:00Z"
}
```

## 오류 코드

### 기술적 오류
- `VALIDATION_ERROR` (400): 요청 검증 실패
- `UNAUTHORIZED` (401): 인증 필요 또는 실패
- `FORBIDDEN` (403): 접근 거부
- `NOT_FOUND` (404): 리소스를 찾을 수 없음
- `CONFLICT` (409): 리소스 충돌
- `INTERNAL_SERVER_ERROR` (500): 내부 시스템 오류

### 도메인 오류
- `BUSINESS_RULE_VIOLATION` (422): 비즈니스 로직 제약 조건 위반
- `EXTERNAL_SERVICE_ERROR` (502): 외부 서비스 장애
- `EXTERNAL_SERVICE_TIMEOUT` (504): 외부 서비스 타임아웃

### GitLab 통합 오류
- `GITLAB_API_ERROR` (502): GitLab API 통신 오류
- `GITLAB_AUTHENTICATION_ERROR` (401): GitLab 토큰 유효하지 않음
- `GITLAB_RATE_LIMIT` (429): GitLab 요청 제한 초과

## 오류 세부사항 스키마

### 필드 검증 오류
```json
{
  "details": {
    "fieldErrors": [
      {
        "field": "name",
        "reason": "must not be blank"
      },
      {
        "field": "email",
        "reason": "must be a valid email address"
      }
    ],
    "correlationId": "abc-123-def"
  }
}
```

### 비즈니스 규칙 위반
```json
{
  "details": {
    "constraint": "UNIQUE_BOOKMARK_URL",
    "entity": {
      "type": "Bookmark",
      "id": "duplicate-url"
    },
    "hint": "A bookmark with this URL already exists"
  }
}
```

### 외부 서비스 오류
```json
{
  "details": {
    "external": {
      "provider": "GitLab",
      "endpoint": "https://gitlab.com/api/v4/projects",
      "httpStatus": 503,
      "responseSnippet": "Service temporarily unavailable"
    },
    "correlationId": "xyz-789-ghi"
  }
}
```

## HTTP 상태 코드 매핑

| 오류 코드 | HTTP 상태 | 설명 |
|-----------|-----------|------|
| `VALIDATION_ERROR` | 400 | Bad Request |
| `UNAUTHORIZED` | 401 | Unauthorized |
| `FORBIDDEN` | 403 | Forbidden |
| `NOT_FOUND` | 404 | Not Found |
| `CONFLICT` | 409 | Conflict |
| `BUSINESS_RULE_VIOLATION` | 422 | Unprocessable Entity |
| `GITLAB_RATE_LIMIT` | 429 | Too Many Requests |
| `INTERNAL_SERVER_ERROR` | 500 | Internal Server Error |
| `EXTERNAL_SERVICE_ERROR` | 502 | Bad Gateway |
| `EXTERNAL_SERVICE_TIMEOUT` | 504 | Gateway Timeout |

## 완전한 오류 예제

### 1. 검증 오류
**요청:**
```http
POST /api/bookmarks
Content-Type: application/json

{
  "name": "",
  "url": "invalid-url"
}
```

**응답:**
```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Request validation failed",
    "details": {
      "fieldErrors": [
        {
          "field": "name",
          "reason": "must not be blank"
        },
        {
          "field": "url",
          "reason": "must be a valid URL"
        }
      ],
      "correlationId": "req-123-abc"
    }
  },
  "timestamp": "2024-08-22T13:00:00Z"
}
```

### 2. 인증 오류
**요청:**
```http
GET /api/bookmarks
X-Api-Key: invalid-key
```

**응답:**
```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "success": false,
  "data": null,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid API key",
    "details": {
      "path": "/api/bookmarks",
      "hint": "Invalid API key",
      "correlationId": "req-456-def"
    }
  },
  "timestamp": "2024-08-22T13:00:00Z"
}
```

### 3. 웹훅 서명 오류
**요청:**
```http
POST /webhook/gitlab
X-Gitlab-Token: invalid-signature
Content-Type: application/json

{
  "event_name": "push"
}
```

**응답:**
```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "success": false,
  "data": null,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "Invalid webhook signature",
    "details": {
      "path": "/webhook/gitlab",
      "correlationId": "req-789-ghi"
    }
  },
  "timestamp": "2024-08-22T13:00:00Z"
}
```

### 4. GitLab API 오류
**요청:**
```http
GET /api/bookmarks/refresh
```

**응답:**
```http
HTTP/1.1 502 Bad Gateway
Content-Type: application/json

{
  "success": false,
  "data": null,
  "error": {
    "code": "EXTERNAL_SERVICE_ERROR",
    "message": "GitLab API communication failed",
    "details": {
      "external": {
        "provider": "GitLab",
        "endpoint": "https://gitlab.com/api/v4/groups/123/projects",
        "httpStatus": 503,
        "responseSnippet": "Service temporarily unavailable"
      },
      "correlationId": "req-101-jkl",
      "hint": "GitLab service may be experiencing issues"
    }
  },
  "timestamp": "2024-08-22T13:00:00Z"
}
```

### 5. 회로 차단기 열림
**요청:**
```http
GET /api/bookmarks
```

**응답:**
```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/json

{
  "success": false,
  "data": null,
  "error": {
    "code": "EXTERNAL_SERVICE_ERROR",
    "message": "GitLab service circuit breaker is open",
    "details": {
      "external": {
        "provider": "GitLab",
        "circuit": "OPEN",
        "nextRetryAt": "2024-08-22T13:01:00Z"
      },
      "correlationId": "req-202-mno",
      "hint": "External service is temporarily unavailable due to repeated failures"
    }
  },
  "timestamp": "2024-08-22T13:00:00Z"
}
```

## 오류 처리 모범 사례

### API 사용자를 위한 지침
1. 항상 `success` 필드를 먼저 확인
2. 지원 요청 시 `correlationId` 사용
3. 다양한 오류 코드를 적절히 처리
4. 지수 백오프를 사용하여 5xx 오류에 대한 재시도 로직 구현
5. `message` 필드의 사용자 친화적 메시지 표시

### 모니터링을 위한 지침
1. 오류율 임계값에 대한 알림 설정
2. 오류 코드 및 빈도 추적
3. 오류와 외부 서비스 상태 간의 상관관계 모니터링
4. 분산 추적을 위한 상관관계 ID 사용

## 오류 응답 헤더
모든 오류 응답에는 디버깅을 위한 다음 헤더가 포함됩니다:
- `X-Correlation-Id`: 요청 상관관계 ID
- `X-Request-Id`: 요청별 ID
- `Content-Type`: application/json

## 지원 및 문제 해결
문제를 보고할 때는 항상 다음을 포함하세요:
- 완전한 오류 응답
- 오류 세부사항의 상관관계 ID
- 오류가 발생한 시간
- 오류를 발생시킨 원본 요청