# Sidebar Backend - 보안 가이드라인

## 개요
이 문서는 Sidebar Backend 애플리케이션의 보안 정책, 절차 및 모범 사례를 설명합니다.

## 인증 및 권한 부여

### API 키 인증
- `SECURITY_API_KEY_ENABLED` 환경 변수를 통해 설정 가능
- 인증을 위해 `X-Api-Key` 헤더 사용
- 제외 엔드포인트: `/actuator/health`, `/swagger-ui/**`, `/webhook/**`

### 웹훅 보안
- GitLab 웹훅에 대한 HMAC-SHA256 서명 검증
- 웹훅 시크릿과 함께 `X-Gitlab-Token` 헤더 사용
- 타이밍 공격을 방지하기 위한 상수 시간 비교

## 시크릿 관리

### 로컬 개발 (Jasypt)
```bash
export JASYPT_ENCRYPTOR_PASSWORD=your-master-key
```

### 프로덕션 (Kubernetes Secrets)
```bash
kubectl create secret generic sidebar-secrets \
  --from-literal=jasypt-password=your-password \
  --from-literal=gitlab-token=your-token
```

## 데이터 보호

### 민감한 정보 마스킹
- 로그에서 비밀번호, 토큰, 신용카드에 대한 자동 마스킹
- 이메일 주소와 전화번호는 부분적으로 마스킹됨
- JWT 토큰은 처음 10자만 표시

### 로깅 보안
- 로그에 민감한 데이터 없음
- 요청 추적을 위한 상관 관계 ID
- 프로덕션에서 JSON 구조화된 로깅

## 네트워크 보안

### HTTPS/TLS
- 모든 외부 통신에 HTTPS 사용
- 인증서 검증 활성화
- TLS 1.2+ 필수

### CORS 설정
- 특정 출처에 대해서만 설정됨
- 프로덕션에서 와일드카드 출처 없음

## 모니터링 및 알림

### 보안 이벤트
- 인증 실패 시도
- 잘못된 웹훅 서명
- 무단 접근 시도
- 회로 차단기 활성화

### 로그 모니터링
```bash
# 보안 이벤트 모니터링
grep "UNAUTHORIZED\|signature.*failed" logs/application.log
```

## 사고 대응

### 보안 사고 유형
1. 무단 접근 시도
2. 데이터 유출 징후
3. 서비스 가용성 문제
4. 의존성 취약점

### 대응 절차
1. 영향받은 시스템 격리
2. 영향 및 범위 평가
3. 차단 조치 구현
4. 사고 문서화 및 보고