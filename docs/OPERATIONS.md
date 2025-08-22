# Sidebar Backend - 운영 런북

## 목차
- [시스템 개요](#시스템-개요)
- [모니터링 및 상태 확인](#모니터링-및-상태-확인)
- [사고 대응](#사고-대응)
- [성능 모니터링](#성능-모니터링)
- [로그 분석](#로그-분석)
- [백업 및 복구](#백업-및-복구)
- [유지보수 절차](#유지보수-절차)
- [설정 관리](#설정-관리)

## 시스템 개요

### 애플리케이션 스택
- **런타임**: Java 24, Spring Boot 3.5.4
- **데이터베이스**: 없음 (GitLab의 파일 기반 데이터)
- **외부 의존성**: GitLab API
- **캐싱**: Spring Cache (Simple/Redis)
- **모니터링**: Spring Boot Actuator + Micrometer

### 주요 구성 요소
- **BookmarkService**: 북마크 데이터 관리를 위한 핵심 비즈니스 로직
- **GitLabService**: 복원력 패턴을 적용한 외부 API 통합
- **WebhookController**: GitLab 웹훅 처리
- **CacheManager**: 설정 가능한 TTL을 가진 데이터 캐싱

## 모니터링 및 상태 확인

### 상태 엔드포인트
```bash
# 애플리케이션 상태
curl http://localhost:8095/actuator/health

# 상세 상태 (인증 필요)
curl -H "X-Api-Key: YOUR_API_KEY" http://localhost:8095/actuator/health

# 준비 상태 프로브 (K8s)
curl http://localhost:8095/actuator/health/readiness

# 생존 상태 프로브 (K8s)
curl http://localhost:8095/actuator/health/liveness
```

### 메트릭 엔드포인트
```bash
# Prometheus 메트릭
curl http://localhost:8095/actuator/prometheus

# 애플리케이션 메트릭
curl http://localhost:8095/actuator/metrics

# 특정 메트릭
curl http://localhost:8095/actuator/metrics/http.server.requests
```

### 모니터링할 주요 메트릭

#### 애플리케이션 메트릭
- `http.server.requests`: HTTP 요청 지연시간 및 상태 코드
- `jvm.memory.used`: JVM 메모리 사용량
- `jvm.gc.pause`: 가비지 컬렉션 메트릭
- `cache.requests`: 캐시 적중/누락 비율

#### 비즈니스 메트릭
- `bookmark.refresh.duration`: 북마크 데이터 새로 고침 시간
- `gitlab.api.requests`: GitLab API 호출 메트릭
- `webhook.processing.duration`: 웹훅 처리 시간

#### 복원력 메트릭
- `resilience4j.circuitbreaker.calls`: 회로 차단기 상태
- `resilience4j.retry.calls`: 재시도 시도
- `resilience4j.bulkhead.calls`: 벌크헤드 사용량

### 알림 임계값
```yaml
# 중요 알림
- HTTP 5xx 오류 > 5분 동안 5%
- JVM 메모리 사용량 > 85%
- GitLab API 회로 차단기 열림
- 애플리케이션 상태 확인 실패

# 경고 알림
- HTTP 지연시간 p95 > 2초
- 캐시 누락 비율 > 50%
- 웹훅 처리 시간 > 30초
```

## 사고 대응

### 일반적인 문제에 대한 런북

#### 1. 높은 오류율 (5xx 오류)
**증상**: 5xx 응답 증가, 상태 확인 실패
```bash
# 애플리케이션 로그 확인
kubectl logs -f deployment/sidebar-backend --tail=100

# 상태 확인
curl http://localhost:8095/actuator/health

# GitLab API 연결성 확인
curl -H "Authorization: Bearer $GITLAB_TOKEN" https://gitlab.com/api/v4/user
```

**해결책**:
1. GitLab API 상태 및 연결성 확인
2. 메트릭에서 회로 차단기 상태 검토
3. 필요시 수평 확장
4. 리소스 제한 확인 (CPU/Memory)

#### 2. GitLab API 회로 차단기 열림
**증상**: 회로 차단기 메트릭이 열림 상태 표시
```bash
# 회로 차단기 메트릭 확인
curl http://localhost:8095/actuator/metrics/resilience4j.circuitbreaker.calls

# GitLab API 상태 확인
curl https://status.gitlab.com/api/v2/status.json
```

**해결책**:
1. GitLab 서비스 상태 확인
2. 네트워크 연결성 확인
3. GitLab API 요청 제한 검토
4. 회로 차단기 자동 복구 대기 (기본 10초)

#### 3. 메모리 문제
**증상**: OutOfMemoryError, 높은 GC 빈도
```bash
# 메모리 메트릭 확인
curl http://localhost:8095/actuator/metrics/jvm.memory.used

# 힙 덤프 생성
jcmd <PID> GC.run_finalization
jcmd <PID> VM.gc
```

**해결책**:
1. JVM 힙 크기 증가 (-Xmx)
2. 메모리 누수 의심시 캐시 정리
3. GitLab API 응답 페이로드 크기 확인
4. 수직 또는 수평 확장

#### 4. 웹훅 처리 실패
**증상**: 웹훅 엔드포인트가 오류 반환
```bash
# 웹훅 관련 로그 확인
kubectl logs -f deployment/sidebar-backend | grep -i webhook

# 웹훅 서명 확인
curl -X POST http://localhost:8095/webhook/gitlab \
  -H "Content-Type: application/json" \
  -H "X-Gitlab-Token: test-token" \
  -d '{"test": "data"}'
```

**해결책**:
1. 웹훅 시크릿 설정 확인
2. 서명 검증 로직 확인
3. GitLab 웹훅 설정 검토
4. 북마크 새로 고침 서비스 상태 확인

## 성능 모니터링

### 주요 성능 지표
- **응답 시간**: p95 < 2초, p99 < 5초
- **처리량**: 지속적으로 100 req/min 처리
- **오류율**: 4xx < 1%, 5xx < 0.1%
- **캐시 적중률**: 북마크 데이터 > 80%

### 성능 튜닝
```bash
# JVM 튜닝
-Xms512m -Xmx2g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
```

```yaml
# 커넥션 풀 튜닝
http:
  client:
    pool-max-connections: 100
    connect-timeout-millis: 3000
    response-timeout-millis: 5000

# 캐시 튜닝
cache:
  ttl: 3600  # 1시간
  enabled: true
```

## 로그 분석

### 로그 구조 (JSON 형식)
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

### 일반적인 로그 쿼리
```bash
# 상관관계 ID로 오류 찾기
grep "correlationId\":\"abc-123" logs/application.log | grep ERROR

# 웹훅 처리 확인
grep "webhook" logs/application.log | jq '.message'

# GitLab API 호출 모니터링
grep "GitLab" logs/application.log | jq '{timestamp, level, message}'

# 보안 알림
grep "UNAUTHORIZED\|signature.*failed" logs/application.log
```

### 환경별 로그 레벨
- **Local**: com.sidebeam은 DEBUG, 기타는 INFO
- **Dev**: com.sidebeam은 INFO, 기타는 WARN  
- **Prod**: com.sidebeam은 WARN, 기타는 ERROR

## 백업 및 복구

### 데이터 소스
- **설정**: GitLab 리포지토리
- **캐시**: 메모리 내 (지속성 불필요)
- **로그**: 순환 방식의 파일 기반

### 백업 전략
```bash
# 설정 백업
git clone https://gitlab.com/your-org/sidebeam-config.git

# 로그 백업 (자동 순환)
# 30일보다 오래된 로그는 자동으로 압축되고 아카이브됨
ls -la logs/application.*.log.gz
```

### 복구 절차
1. **서비스 복구**:
   ```bash
   # Kubernetes
   kubectl rollout restart deployment/sidebar-backend
   
   # Docker
   docker restart sidebar-backend
   ```

2. **설정 복구**:
   ```bash
   # Git에서 복구
   git checkout <known-good-commit>
   
   # 설정 재배포
   kubectl apply -f k8s/configmap.yaml
   ```

3. **캐시 복구**:
   - 캐시는 애플리케이션 시작시 자동으로 재구축됨
   - 수동 새로 고침: `POST /api/bookmarks/refresh` (인증 필요)

## 유지보수 절차

### 정기 유지보수
- **주간**: 메트릭 및 알림 검토
- **월간**: 로그 순환 및 정리
- **분기**: 성능 검토 및 튜닝

### 애플리케이션 업데이트
```bash
# 무중단 배포
kubectl set image deployment/sidebar-backend \
  app=sidebar-backend:new-version

# 필요시 롤백
kubectl rollout undo deployment/sidebar-backend
```

### 설정 업데이트
```bash
# ConfigMap 업데이트
kubectl create configmap sidebar-config \
  --from-file=application.yml --dry-run=client -o yaml | \
  kubectl apply -f -

# 새 설정을 적용하기 위해 파드 재시작
kubectl rollout restart deployment/sidebar-backend
```

### 보안 업데이트
1. build.gradle.kts의 의존성 업데이트
2. 보안 스캔 실행
3. 스테이징 환경에서 테스트
4. 무중단 배포
5. 회귀 모니터링

## 설정 관리

### 환경 변수
```bash
# 필수
JASYPT_ENCRYPTOR_PASSWORD=your-master-password
GITLAB_BRANCH=main

# 선택사항
SECURITY_API_KEY_ENABLED=true
SECURITY_API_KEY_VALUE=your-api-key
LOG_LEVEL_COM_SIDEBEAM=INFO
```

### 설정 프로파일
- **local**: 콘솔 로깅이 있는 개발
- **dev**: JSON 로깅이 있는 개발 환경
- **staging**: 프로덕션 전 테스트
- **prod**: 완전한 모니터링이 있는 프로덕션

### 시크릿 관리
```bash
# 로컬 개발 (Jasypt)
export JASYPT_ENCRYPTOR_PASSWORD=your-password

# 프로덕션 (Kubernetes Secrets)
kubectl create secret generic sidebar-secrets \
  --from-literal=jasypt-password=your-password \
  --from-literal=gitlab-token=your-token
```

## 비상 연락처

- **당직 엔지니어**: [연락처 정보]
- **기술 리드**: [기술 리드 연락처]
- **인프라 팀**: [인프라 팀 연락처]
- **GitLab 지원**: https://about.gitlab.com/support/

## 추가 자료

- [애플리케이션 아키텍처](./ARCHITECTURE.md)
- [보안 가이드라인](./SECURITY.md)
- [API 문서](../README.md)
- [오류 응답 스키마](./ERROR_RESPONSES.md)