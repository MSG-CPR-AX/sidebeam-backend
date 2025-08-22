# Sidebar Backend - Security Guidelines

## Overview
This document outlines security policies, procedures, and best practices for the Sidebar Backend application.

## Authentication & Authorization

### API Key Authentication
- Configurable via `SECURITY_API_KEY_ENABLED` environment variable
- Uses `X-Api-Key` header for authentication
- Excluded endpoints: `/actuator/health`, `/swagger-ui/**`, `/webhook/**`

### Webhook Security
- HMAC-SHA256 signature verification for GitLab webhooks
- Uses `X-Gitlab-Token` header with webhook secret
- Constant-time comparison to prevent timing attacks

## Secret Management

### Local Development (Jasypt)
```bash
export JASYPT_ENCRYPTOR_PASSWORD=your-master-key
```

### Production (Kubernetes Secrets)
```bash
kubectl create secret generic sidebar-secrets \
  --from-literal=jasypt-password=your-password \
  --from-literal=gitlab-token=your-token
```

## Data Protection

### Sensitive Information Masking
- Automatic masking in logs for passwords, tokens, credit cards
- Email addresses and phone numbers are partially masked
- JWT tokens show only first 10 characters

### Logging Security
- No sensitive data in logs
- Correlation IDs for request tracing
- JSON structured logging in production

## Network Security

### HTTPS/TLS
- All external communications use HTTPS
- Certificate validation enabled
- TLS 1.2+ required

### CORS Configuration
- Configured for specific origins only
- No wildcard origins in production

## Monitoring & Alerting

### Security Events
- Failed authentication attempts
- Invalid webhook signatures
- Unauthorized access attempts
- Circuit breaker activations

### Log Monitoring
```bash
# Monitor security events
grep "UNAUTHORIZED\|signature.*failed" logs/application.log
```

## Incident Response

### Security Incident Types
1. Unauthorized access attempts
2. Data breach indicators  
3. Service availability issues
4. Dependency vulnerabilities

### Response Procedures
1. Isolate affected systems
2. Assess impact and scope
3. Implement containment measures
4. Document and report incident