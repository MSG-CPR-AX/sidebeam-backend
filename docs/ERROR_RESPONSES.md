# Error Response Schema Documentation

## Overview
This document describes the standardized error response format used throughout the Sidebar Backend API, including error codes, response structure, and examples.

## Standard Response Format

### Success Response
```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2024-08-22T13:00:00Z"
}
```

### Error Response
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

## Error Codes

### Technical Errors
- `VALIDATION_ERROR` (400): Request validation failed
- `UNAUTHORIZED` (401): Authentication required or failed
- `FORBIDDEN` (403): Access denied
- `NOT_FOUND` (404): Resource not found
- `CONFLICT` (409): Resource conflict
- `INTERNAL_SERVER_ERROR` (500): Internal system error

### Domain Errors
- `BUSINESS_RULE_VIOLATION` (422): Business logic constraint violated
- `EXTERNAL_SERVICE_ERROR` (502): External service failure
- `EXTERNAL_SERVICE_TIMEOUT` (504): External service timeout

### GitLab Integration Errors
- `GITLAB_API_ERROR` (502): GitLab API communication error
- `GITLAB_AUTHENTICATION_ERROR` (401): GitLab token invalid
- `GITLAB_RATE_LIMIT` (429): GitLab rate limit exceeded

## Error Details Schema

### Field Validation Errors
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

### Business Rule Violations
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

### External Service Errors
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

## HTTP Status Code Mapping

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
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

## Complete Error Examples

### 1. Validation Error
**Request:**
```http
POST /api/bookmarks
Content-Type: application/json

{
  "name": "",
  "url": "invalid-url"
}
```

**Response:**
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

### 2. Authentication Error
**Request:**
```http
GET /api/bookmarks
X-Api-Key: invalid-key
```

**Response:**
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

### 3. Webhook Signature Error
**Request:**
```http
POST /webhook/gitlab
X-Gitlab-Token: invalid-signature
Content-Type: application/json

{
  "event_name": "push"
}
```

**Response:**
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

### 4. GitLab API Error
**Request:**
```http
GET /api/bookmarks/refresh
```

**Response:**
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

### 5. Circuit Breaker Open
**Request:**
```http
GET /api/bookmarks
```

**Response:**
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

## Error Handling Best Practices

### For API Consumers
1. Always check the `success` field first
2. Use `correlationId` for support requests
3. Handle different error codes appropriately
4. Implement retry logic for 5xx errors with exponential backoff
5. Display user-friendly messages from the `message` field

### For Monitoring
1. Alert on error rate thresholds
2. Track error codes and their frequencies
3. Monitor correlation between errors and external service health
4. Use correlation IDs for distributed tracing

## Error Response Headers
All error responses include these headers for debugging:
- `X-Correlation-Id`: Request correlation ID
- `X-Request-Id`: Request-specific ID
- `Content-Type`: application/json

## Support and Troubleshooting
When reporting issues, always include:
- The complete error response
- The correlation ID from the error details
- The timestamp when the error occurred
- The original request that caused the error