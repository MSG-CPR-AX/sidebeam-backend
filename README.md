# Sidebar Backend

Spring Boot 3 기반의 사이드바 백엔드 애플리케이션입니다.

## 📋 목차

- [시작하기](#시작하기)
- [Jasypt 암호화 설정 (로컬 개발환경)](#jasypt-암호화-설정-로컬-개발환경)
- [개발 가이드](#개발-가이드)
- [API 문서](#api-문서)
- [트러블슈팅](#트러블슈팅)

---

## 🚀 시작하기

### 사전 요구사항

- Java 21
- Gradle 8.x
- GitLab 개인 액세스 토큰 (read_repository 권한)

### 기본 실행

```bash
./gradlew bootRun
```

애플리케이션은 http://localhost:8095 에서 실행됩니다.

---

## 🔐 Jasypt 암호화 설정 (로컬 개발환경)

로컬 개발환경에서는 민감한 설정값들이 Jasypt를 통해 암호화되어 관리됩니다.

### 1. 환경변수 설정

로컬에서 애플리케이션을 실행하기 전에 반드시 마스터 키를 설정해야 합니다:

```bash
export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
```

#### IDE에서 실행하는 경우

IntelliJ IDEA 또는 다른 IDE에서 실행할 때는 환경변수를 설정하세요:

- **IntelliJ IDEA**: Run Configuration > Environment Variables 섹션에 추가
  ```
  JASYPT_ENCRYPTOR_PASSWORD=your-local-master-key
  ```

### 2. 암호화가 필요한 속성값들

다음 속성값들이 Jasypt로 암호화되어 있습니다:

- `gitlab.access-token`: GitLab API 액세스 토큰
- `gitlab.root-group-id`: GitLab 루트 그룹 ID
- `webhook.secret-token`: GitLab 웹훅 시크릿 토큰

### 3. 새로운 값 암호화 방법

#### 방법 1: 단일 값 암호화

```bash
# 1. 환경변수 설정
export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'

# 2. 테스트를 통해 암호화
./gradlew test \
  -Dspring.profiles.active=local \
  -Djasypt.value='암호화할-평문-값' \
  --tests '*JasyptEncryptorTest.printEncrypted'

# 3. 테스트 로그에서 ENC(...) 값 확인
# 출력 예시: 암호문: ENC(XxXxXxXxXxXx...)
```

#### 방법 2: 여러 값을 한번에 암호화

1. `JasyptEncryptorTest.java`의 `encryptMultipleValues()` 메서드에서 `valuesToEncrypt` 배열에 실제 값들을 입력
2. 테스트 실행:
   ```bash
   export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
   ./gradlew test \
     -Dspring.profiles.active=local \
     --tests '*JasyptEncryptorTest.encryptMultipleValues'
   ```

### 4. application.yml 업데이트

암호화된 값을 다음과 같이 `application.yml`에 적용하세요:

```yaml
gitlab:
  access-token: ENC(your-encrypted-gitlab-access-token)
  root-group-id: ENC(your-encrypted-gitlab-root-group-id)

webhook:
  secret-token: ENC(your-encrypted-webhook-secret-token)
```

### 5. 로컬 실행

환경변수를 설정한 후 애플리케이션을 실행하세요:

```bash
export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
./gradlew bootRun
```

#### 성공적인 실행 로그

```
✅ [LOCAL] Jasypt 암호화 키가 정상적으로 설정되었습니다.
```

#### 환경변수 미설정 시 에러

```
❌ [LOCAL] JASYPT_ENCRYPTOR_PASSWORD 환경변수가 설정되지 않았습니다!
===============================================================
로컬 개발환경에서 암호화된 설정값을 사용하려면 다음과 같이 설정하세요:

1. 환경변수 설정:
   export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'

2. 애플리케이션 재실행:
   ./gradlew bootRun
===============================================================
```

### 6. 복호화 검증

다음 테스트로 복호화가 정상적으로 작동하는지 확인할 수 있습니다:

```bash
export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
./gradlew test \
  -Dspring.profiles.active=local \
  --tests '*JasyptLocalProfileTest'
```

---

## 🛠️ 개발 가이드

### 프로젝트 구조

```
src/
├── main/java/com/sidebeam/
│   ├── bookmark/           # 북마크 관련 기능
│   ├── config/             # 설정 클래스
│   │   └── security/       # Jasypt 보안 설정
│   ├── common/             # 공통 유틸리티
│   └── external/           # 외부 API 연동
├── main/resources/
│   ├── application.yml     # 애플리케이션 설정 (암호화된 값 포함)
│   └── ...
└── test/                   # 테스트 코드
    └── java/com/sidebeam/config/security/
        ├── JasyptEncryptorTest.java      # 암호화 유틸리티
        └── JasyptLocalProfileTest.java   # 복호화 검증
```

### Jasypt 관련 주요 파일

- **LocalJasyptGuard.java**: 로컬 프로파일에서 환경변수 검증 및 fail-fast
- **JasyptEncryptorTest.java**: 평문을 암호화하는 테스트 유틸리티  
- **JasyptLocalProfileTest.java**: 복호화 기능 검증 테스트
- **application.yml**: Jasypt 설정 및 암호화된 속성값들

### 새로운 암호화 속성 추가

1. `application.yml`에 새 속성을 `ENC(...)` 형태로 추가
2. `JasyptEncryptorTest`로 실제 값을 암호화
3. `JasyptLocalProfileTest`에 새 속성의 검증 로직 추가
4. 테스트 실행으로 정상 작동 확인

### 비로컬 환경에서의 동작

- **Dev/Stage/Prod/Test** 환경에서는 Jasypt가 비활성화됩니다
- `LocalJasyptGuard`는 `@Profile("local")`로 제한되어 다른 환경에 영향을 주지 않습니다
- 비로컬 환경에서는 기존 환경변수 방식을 계속 사용할 수 있습니다

---

## 📚 API 문서

애플리케이션 실행 후 다음 URL에서 API 문서를 확인할 수 있습니다:

- **Swagger UI**: http://localhost:8095/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8095/api-docs

---

## 🔧 트러블슈팅

### Jasypt 관련 문제

#### 1. 애플리케이션이 시작 직후 종료되는 경우

**증상**: 로그에 "JASYPT_ENCRYPTOR_PASSWORD 미설정" 메시지

**해결방법**:
```bash
export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
```

#### 2. 복호화 실패 (Bad Password 에러)

**증상**: 
```
org.jasypt.exceptions.EncryptionOperationNotPossibleException: Encryption raised an exception
```

**원인**: 
- 잘못된 마스터 키 사용
- 다른 알고리즘으로 암호화된 값 사용

**해결방법**:
1. 올바른 마스터 키 확인
2. 동일한 설정으로 다시 암호화:
   ```bash
   ./gradlew test \
     -Dspring.profiles.active=local \
     -Djasypt.value='올바른-평문-값' \
     --tests '*JasyptEncryptorTest.printEncrypted'
   ```

#### 3. 속성값이 암호화된 상태로 주입되는 경우

**증상**: `@Value`로 주입된 값이 `ENC(...)`로 시작

**원인**: 
- Jasypt가 활성화되지 않음
- 마스터 키 미설정

**해결방법**:
1. `spring.profiles.active=local` 설정 확인
2. 환경변수 `JASYPT_ENCRYPTOR_PASSWORD` 설정 확인
3. `JasyptLocalProfileTest` 실행으로 검증

#### 4. 테스트 실행 시 StringEncryptor 빈을 찾을 수 없는 경우

**증상**: 
```
No qualifying bean of type 'org.jasypt.encryption.StringEncryptor'
```

**해결방법**:
1. 테스트에 `@ActiveProfiles("local")` 추가 확인
2. `JASYPT_ENCRYPTOR_PASSWORD` 환경변수 설정 확인

### 일반적인 문제

#### GitLab API 연동 문제

**증상**: GitLab API 호출 실패

**확인사항**:
1. `gitlab.access-token`이 올바르게 복호화되었는지 확인
2. 토큰에 `read_repository` 권한이 있는지 확인
3. `gitlab.api-url`이 올바른 GitLab 인스턴스를 가리키는지 확인

---

## 📞 지원

문제가 지속되는 경우:

1. **로그 확인**: 애플리케이션 시작 로그에서 Jasypt 관련 메시지 확인
2. **테스트 실행**: `JasyptLocalProfileTest`로 복호화 상태 검증
3. **환경변수 확인**: `echo $JASYPT_ENCRYPTOR_PASSWORD`로 설정 상태 확인

---

## 🔒 보안 주의사항

- **마스터 키 관리**: `JASYPT_ENCRYPTOR_PASSWORD`는 반드시 안전하게 보관하세요
- **버전 관리**: 암호화된 값은 Git에 커밋해도 안전하지만, 마스터 키는 절대 커밋하지 마세요
- **로그 보안**: 민감한 값이 로그에 출력되지 않도록 `maskSensitiveValue()` 등의 유틸리티를 사용하세요
- **환경 분리**: 로컬 환경의 마스터 키와 실제 운영 환경의 암호화 방식은 별도로 관리하세요