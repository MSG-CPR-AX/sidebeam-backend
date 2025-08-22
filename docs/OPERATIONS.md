# Sidebar Backend - Operations Runbook

## Table of Contents
- [System Overview](#system-overview)
- [Monitoring & Health Checks](#monitoring--health-checks)
- [Incident Response](#incident-response)
- [Performance Monitoring](#performance-monitoring)
- [Log Analysis](#log-analysis)
- [Backup & Recovery](#backup--recovery)
- [Maintenance Procedures](#maintenance-procedures)
- [Configuration Management](#configuration-management)

## System Overview

### Application Stack
- **Runtime**: Java 24, Spring Boot 3.5.4
- **Database**: None (File-based data from GitLab)
- **External Dependencies**: GitLab API
- **Caching**: Spring Cache (Simple/Redis)
- **Monitoring**: Spring Boot Actuator + Micrometer

### Key Components
- **BookmarkService**: Core business logic for bookmark data management
- **GitLabService**: External API integration with resilience patterns
- **WebhookController**: GitLab webhook processing
- **CacheManager**: Data caching with configurable TTL

## Monitoring & Health Checks

### Health Endpoints
```bash
# Application health
curl http://localhost:8095/actuator/health

# Detailed health (with authorization)
curl -H "X-Api-Key: YOUR_API_KEY" http://localhost:8095/actuator/health

# Readiness probe (K8s)
curl http://localhost:8095/actuator/health/readiness

# Liveness probe (K8s)
curl http://localhost:8095/actuator/health/liveness
```

### Metrics Endpoints
```bash
# Prometheus metrics
curl http://localhost:8095/actuator/prometheus

# Application metrics
curl http://localhost:8095/actuator/metrics

# Specific metric
curl http://localhost:8095/actuator/metrics/http.server.requests
```

### Key Metrics to Monitor

#### Application Metrics
- `http.server.requests`: HTTP request latency and status codes
- `jvm.memory.used`: JVM memory usage
- `jvm.gc.pause`: Garbage collection metrics
- `cache.requests`: Cache hit/miss ratios

#### Business Metrics
- `bookmark.refresh.duration`: Bookmark data refresh time
- `gitlab.api.requests`: GitLab API call metrics
- `webhook.processing.duration`: Webhook processing time

#### Resilience Metrics
- `resilience4j.circuitbreaker.calls`: Circuit breaker state
- `resilience4j.retry.calls`: Retry attempts
- `resilience4j.bulkhead.calls`: Bulkhead usage

### Alerting Thresholds
```yaml
# Critical Alerts
- HTTP 5xx errors > 5% over 5 minutes
- JVM memory usage > 85%
- GitLab API circuit breaker OPEN
- Application health check failing

# Warning Alerts
- HTTP latency p95 > 2 seconds
- Cache miss ratio > 50%
- Webhook processing time > 30 seconds
```

## Incident Response

### Runbook for Common Issues

#### 1. High Error Rate (5xx Errors)
**Symptoms**: Increased 5xx responses, failing health checks
```bash
# Check application logs
kubectl logs -f deployment/sidebar-backend --tail=100

# Check health status
curl http://localhost:8095/actuator/health

# Check GitLab API connectivity
curl -H "Authorization: Bearer $GITLAB_TOKEN" https://gitlab.com/api/v4/user
```

**Resolution**:
1. Check GitLab API status and connectivity
2. Review circuit breaker state in metrics
3. Scale horizontally if needed
4. Check resource limits (CPU/Memory)

#### 2. GitLab API Circuit Breaker Open
**Symptoms**: Circuit breaker metrics show OPEN state
```bash
# Check circuit breaker metrics
curl http://localhost:8095/actuator/metrics/resilience4j.circuitbreaker.calls

# Check GitLab API health
curl https://status.gitlab.com/api/v2/status.json
```

**Resolution**:
1. Verify GitLab service status
2. Check network connectivity
3. Review GitLab API rate limits
4. Wait for circuit breaker to auto-recover (10s default)

#### 3. Memory Issues
**Symptoms**: OutOfMemoryError, high GC frequency
```bash
# Check memory metrics
curl http://localhost:8095/actuator/metrics/jvm.memory.used

# Generate heap dump
jcmd <PID> GC.run_finalization
jcmd <PID> VM.gc
```

**Resolution**:
1. Increase JVM heap size (-Xmx)
2. Clear cache if memory leak suspected
3. Check for GitLab API response payload size
4. Scale vertically or horizontally

#### 4. Webhook Processing Failures
**Symptoms**: Webhook endpoints returning errors
```bash
# Check webhook-specific logs
kubectl logs -f deployment/sidebar-backend | grep -i webhook

# Verify webhook signature
curl -X POST http://localhost:8095/webhook/gitlab \
  -H "Content-Type: application/json" \
  -H "X-Gitlab-Token: test-token" \
  -d '{"test": "data"}'
```

**Resolution**:
1. Verify webhook secret configuration
2. Check signature verification logic
3. Review GitLab webhook configuration
4. Check bookmark refresh service health

## Performance Monitoring

### Key Performance Indicators
- **Response Time**: p95 < 2s, p99 < 5s
- **Throughput**: Handle 100 req/min sustained
- **Error Rate**: < 1% for 4xx, < 0.1% for 5xx
- **Cache Hit Rate**: > 80% for bookmark data

### Performance Tuning
```bash
# JVM Tuning
-Xms512m -Xmx2g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
```

```yaml
# Connection Pool Tuning
http:
  client:
    pool-max-connections: 100
    connect-timeout-millis: 3000
    response-timeout-millis: 5000

# Cache Tuning
cache:
  ttl: 3600  # 1 hour
  enabled: true
```

## Log Analysis

### Log Structure (JSON Format)
```json
{
  "timestamp": "2024-01-01T00:00:00.000Z",
  "level": "INFO",
  "message": "Request completed",
  "correlationId": "abc-123",
  "requestId": "def-456",
  "service": "sidebar-backend",
  "logger": "com.sidebeam.filter.CorrelationIdFilter"
}
```

### Common Log Queries
```bash
# Find errors by correlation ID
grep "correlationId\":\"abc-123" logs/application.log | grep ERROR

# Check webhook processing
grep "webhook" logs/application.log | jq '.message'

# Monitor GitLab API calls
grep "GitLab" logs/application.log | jq '{timestamp, level, message}'

# Security alerts
grep "UNAUTHORIZED\|signature.*failed" logs/application.log
```

### Log Levels by Environment
- **Local**: DEBUG for com.sidebeam, INFO for others
- **Dev**: INFO for com.sidebeam, WARN for others  
- **Prod**: WARN for com.sidebeam, ERROR for others

## Backup & Recovery

### Data Sources
- **Configuration**: GitLab repositories
- **Cache**: In-memory (no persistence needed)
- **Logs**: File-based with rotation

### Backup Strategy
```bash
# Configuration backup
git clone https://gitlab.com/your-org/sidebeam-config.git

# Log backup (automated rotation)
# Logs older than 30 days are automatically compressed and archived
ls -la logs/application.*.log.gz
```

### Recovery Procedures
1. **Service Recovery**:
   ```bash
   # Kubernetes
   kubectl rollout restart deployment/sidebar-backend
   
   # Docker
   docker restart sidebar-backend
   ```

2. **Configuration Recovery**:
   ```bash
   # Restore from Git
   git checkout <known-good-commit>
   
   # Redeploy configuration
   kubectl apply -f k8s/configmap.yaml
   ```

3. **Cache Recovery**:
   - Cache is automatically rebuilt on application startup
   - Manual refresh: `POST /api/bookmarks/refresh` (authenticated)

## Maintenance Procedures

### Routine Maintenance
- **Weekly**: Review metrics and alerts
- **Monthly**: Log rotation and cleanup
- **Quarterly**: Performance review and tuning

### Application Updates
```bash
# Zero-downtime deployment
kubectl set image deployment/sidebar-backend \
  app=sidebar-backend:new-version

# Rollback if needed
kubectl rollout undo deployment/sidebar-backend
```

### Configuration Updates
```bash
# Update ConfigMap
kubectl create configmap sidebar-config \
  --from-file=application.yml --dry-run=client -o yaml | \
  kubectl apply -f -

# Restart pods to pick up new config
kubectl rollout restart deployment/sidebar-backend
```

### Security Updates
1. Update dependencies in build.gradle.kts
2. Run security scans
3. Test in staging environment
4. Deploy with zero downtime
5. Monitor for regressions

## Configuration Management

### Environment Variables
```bash
# Required
JASYPT_ENCRYPTOR_PASSWORD=your-master-password
GITLAB_BRANCH=main

# Optional
SECURITY_API_KEY_ENABLED=true
SECURITY_API_KEY_VALUE=your-api-key
LOG_LEVEL_COM_SIDEBEAM=INFO
```

### Configuration Profiles
- **local**: Development with console logging
- **dev**: Development environment with JSON logging
- **staging**: Pre-production testing
- **prod**: Production with full monitoring

### Secret Management
```bash
# Local development (Jasypt)
export JASYPT_ENCRYPTOR_PASSWORD=your-password

# Production (Kubernetes Secrets)
kubectl create secret generic sidebar-secrets \
  --from-literal=jasypt-password=your-password \
  --from-literal=gitlab-token=your-token
```

## Emergency Contacts

- **On-call Engineer**: [Your contact info]
- **Tech Lead**: [Tech lead contact]
- **Infrastructure Team**: [Infra team contact]
- **GitLab Support**: https://about.gitlab.com/support/

## Additional Resources

- [Application Architecture](./ARCHITECTURE.md)
- [Security Guidelines](./SECURITY.md)
- [API Documentation](../README.md)
- [Error Response Schema](./ERROR_RESPONSES.md)