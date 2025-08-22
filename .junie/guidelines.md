## 일반 원칙

* **보수성:** 새로운 외부 라이브러리/프레임워크 추가 금지(명시 요청 시만).
* **컴포넌트 기반 개발(CBD):** 중복/공통 로직을 **작은 컴포넌트**로 모듈화하고, 상위 **Facade(파사드)** 컴포넌트가 이들을 **조합**하여 기능을 완성한다.
* **층 분리:** Controller ↔ Facade(Service) ↔ Repository/Adapter. 비즈니스 조합은 Facade에서만.
* **DTO 우선:** API 입출력은 DTO만 사용(엔티티 노출 금지).
* **검증 선행:** 요청 DTO에 Bean Validation. 실패 시 **공통 응답**으로 오류 반환.
* **예외 일원화:** 예외 → ErrorCode → HttpStatus → **공통 응답** 매핑.
* **트랜잭션:** Facade(Service) 계층에 `@Transactional` (조회는 `readOnly = true`).
* **로깅:** 에러 로그에 컨텍스트(요청/상관관계 ID 등), 민감정보 금지.
* **명확성:** 이름은 역할이 드러나게, 주석은 *왜*에 집중.

---

## 아키텍처 & 패키징 (CBD + Facade)

권장 기본 구조:

```
com.example.project
├─ api              // controllers, request/response DTOs
├─ application      // facades/services (transactional orchestration)
├─ component        // small reusable components (validators, mappers, calculators, policies, converters, enrichers ...)
├─ domain           // entities/aggregates, domain services, repository ports
└─ infrastructure   // JPA adapters, external clients, configs
```

### 컴포넌트(작은 컴포넌트) 원칙

* **단일 책임:** 한 가지 일을 작고 명확하게.
* **조립 가능:** 입출력 타입을 명확히 하고 **부작용 최소화**(가능하면 무상태).
* **재사용:** 상위 Facade가 composable 하게 조합할 수 있도록 의존성은 **생성자 주입**.
* **경계:** DB/네트워크 접근은 전용 Adapter 컴포넌트에 한정.
* **명명 규칙 예:** `XxxValidator`, `XxxMapper`, `XxxCalculator`, `XxxPolicy`, `XxxConverter`, `XxxEnricher`.

### Facade(파사드) 원칙

* **오케스트레이션:** 여러 작은 컴포넌트를 **순서/규칙**에 따라 조합.
* **트랜잭션 경계:** Facade public 메서드에 `@Transactional`.
* **DTO↔도메인 변환**은 전용 컴포넌트/매퍼로 위임(서비스/컨트롤러에 로직 금지).
* **예외 변환:** 내부 예외를 ErrorCode로 변환해 상위 계층으로 전달.

---

## REST API 가이드

* **URI:** 리소스 중심, 복수형. 예) `/api/v1/orders/{id}`
* **HTTP 메서드:** GET/POST/PUT/PATCH/DELETE 의미 준수
* **상태코드:** 200/201/204 성공, 400 검증, 401/403 인증/인가, 404 없음, 409 충돌, 422 비즈니스 규칙 위반
* **페이징:** `page`, `size`, `sort` 파라미터. 응답에 `totalElements`, `totalPages`.
* **요청 검증:** Bean Validation + 필드 오류 상세 제공.

---

## 공통 응답(Common Response) 지침

**1) 먼저 탐색:** 프로젝트에 **이미 정의된 공통 응답/에러 모델**이 있는지 확인한다.

* 후보 클래스/패턴: `ApiResponse`, `CommonResponse`, `BaseResponse`, `ErrorResponse`, `ErrorCode`, `GlobalExceptionHandler`, `@RestControllerAdvice`.
* 존재할 경우: **필드명/구조/에러코드 체계**를 그대로 **준수**해서 개발한다.

**2) 미정의 시, 아래 기본 스펙을 정의하고 사용한다:**

* 성공:

  ```json
  {
    "success": true,
    "data": { },
    "error": null,
    "timestamp": "ISO-8601"
  }
  ```
* 실패:

  ```json
  {
    "success": false,
    "data": null,
    "error": {
      "code": "DOMAIN_XXX",
      "message": "Human-friendly message",
      "details": {
        "fieldErrors": [
          {"field":"name","reason":"must not be blank"}
        ]
      }
    },
    "timestamp": "ISO-8601"
  }
  ```

**기본 구현 스케치 (필요 시 생성):**

```java
public record ApiResponse<T>(boolean success, T data, ErrorResponse error, Instant timestamp) {
  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, data, null, Instant.now());
  }
  public static <T> ApiResponse<T> error(ErrorResponse error) {
    return new ApiResponse<>(false, null, error, Instant.now());
  }
}

public record ErrorResponse(String code, String message, Map<String, Object> details) {
  public static ErrorResponse of(ErrorCode code, String message, Map<String, Object> details) {
    return new ErrorResponse(code.name(), message != null ? message : code.getDefaultMessage(), details);
  }
}

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Invalid request"),
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "Entity not found"),
  CONFLICT(HttpStatus.CONFLICT, "Conflicting state"),
  BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated"),
  EXTERNAL_SERVICE_ERROR(HttpStatus.BAD_GATEWAY, "External service error");
  private final HttpStatus status;
  private final String defaultMessage;
}

@RestControllerAdvice
@RequiredArgsConstructor
class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
    var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
      .map(fe -> Map.of("field", fe.getField(), "reason", fe.getDefaultMessage()))
      .toList();
    var err = ErrorResponse.of(ErrorCode.VALIDATION_ERROR, null, Map.of("fieldErrors", fieldErrors));
    return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus()).body(ApiResponse.error(err));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
    var err = ErrorResponse.of(ErrorCode.ENTITY_NOT_FOUND, ex.getMessage(), Map.of());
    return ResponseEntity.status(ErrorCode.ENTITY_NOT_FOUND.getStatus()).body(ApiResponse.error(err));
  }

  // add business exceptions as needed...
}
```

---

## 코드 컨벤션

* **Java/Lombok:** LTS 기준(Java 17+ 권장), `@RequiredArgsConstructor` 선호, 엔티티에 `@Data` 지양.
* **Optional:** 리포지토리 반환에 한정. 파라미터로 사용 지양.
* **메서드 명:** command/query 의미 분리.
* **불변식:** 팩토리/정적 생성으로 보장.
* **주석:** 공개 API/의도 설명 위주.

---

## 영속성(JPA/Hibernate)

* **엔티티:** 보호 생성자, 식별자/상태 일관성 보장.
* **리포지토리:** 도메인 의미 기반 시그니처. 서비스에 JPA 세부 누수 금지.
* **쿼리:** 파생 쿼리/JPQL 우선, N+1 방지(fetch join/entity graph).
* **마이그레이션:** (존재 시) Flyway/Liquibase, 변경당 1 파일.

---

## 테스트 전략 — **BDD 우선**

### 원칙

* **스타일:** BDD(Given–When–Then) 네이밍/서술.
* **구조:** AAA를 BDD 문맥에 맞춰 사용(Arrange→Act→Assert = Given→When→Then).
* **목킹:** 외부 의존성만 목킹. **작은 컴포넌트는 가능한 실제 구현**으로 검증(순수 함수화 권장).
* **커버리지:** 정상/경계/에러 흐름, 검증 실패, 정책 위반, 중복 로직 제거 확인.

### 컨트롤러 테스트(예시, MockMvc + BDD)

```java
@AutoConfigureMockMvc
@WebMvcTest(controllers = SampleController.class)
class SampleControllerTest {

  @Autowired MockMvc mockMvc;
  @MockBean SampleFacade facade;

  @Test
  void givenInvalidRequest_whenCreate_then400WithFieldErrors() throws Exception {
    // Given
    var req = """
      {"name":""}
    """;

    // When / Then
    mockMvc.perform(post("/api/v1/samples")
        .contentType(MediaType.APPLICATION_JSON)
        .content(req))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.success").value(false))
      .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
      .andExpect(jsonPath("$.error.details.fieldErrors[0].field").value("name"));
  }
}
```

### Facade(Service) 테스트(예시, BDDMockito)

```java
@ExtendWith(MockitoExtension.class)
class SampleFacadeTest {

  @Mock NameDuplicationPolicy duplicationPolicy;  // 작은 컴포넌트(정책)
  @Mock NameNormalizer normalizer;               // 작은 컴포넌트(정규화)
  @Mock SampleRepository repo;                   // 포트/어댑터
  @InjectMocks SampleFacade sut;

  @Test
  void givenDuplicateName_whenCreate_thenThrowsConflict() {
    // Given
    given(normalizer.normalize(" ABC ")).willReturn("abc");
    given(duplicationPolicy.exists("abc")).willReturn(true);

    // When / Then
    assertThatThrownBy(() -> sut.create(new CreateCommand(" ABC ")))
      .isInstanceOf(DomainConflictException.class)
      .hasMessageContaining("already exists");
  }

  @Test
  void givenValidRequest_whenCreate_thenPersistsAndReturnsDto() {
    // Given
    given(normalizer.normalize("abc")).willReturn("abc");
    given(duplicationPolicy.exists("abc")).willReturn(false);
    given(repo.save(any())).willAnswer(inv -> {
      var e = (SampleEntity) inv.getArgument(0);
      e.setId(1L);
      return e;
    });

    // When
    var dto = sut.create(new CreateCommand("abc"));

    // Then
    then(repo).should().save(any(SampleEntity.class));
    assertThat(dto.id()).isEqualTo(1L);
  }
}
```

### 작은 컴포넌트 테스트(순수 단위)

```java
class NameNormalizerTest {
  @Test
  void givenWhitespaceAndCase_whenNormalize_thenTrimAndLowercase() {
    var normalizer = new NameNormalizer();
    assertThat(normalizer.normalize(" AbC ")).isEqualTo("abc");
  }
}
```

### 리포지토리 테스트(`@DataJpaTest`)

```java
@DataJpaTest
class SampleRepositoryTest {

  @Autowired TestEntityManager em;
  @Autowired SampleRepository repo;

  @Test
  void givenEntity_whenSaveAndFind_thenReturnsPersisted() {
    var e = new SampleEntity(null, "abc");
    var saved = repo.save(e);

    var found = repo.findById(saved.getId()).orElseThrow();
    assertThat(found.getName()).isEqualTo("abc");
  }
}
```

---

## Junie 작업 규칙

* **생성/수정 산출물**

    1. (필요 시) Controller + Request/Response DTO
    2. Facade(Service) + 작은 컴포넌트 조합 로직(트랜잭션 포함)
    3. Repository/Adapter 메서드 (서비스에 JPA 세부 누수 금지)
    4. 테스트: **BDD 스타일**의 Controller/Facade/Component/Repository 테스트
    5. 예외 경로 + 검증 애너테이션 + 공통 응답 적용(위 지침 따름)

* **공통 응답 적용 순서**

    1. **기존 정의 탐색 → 그대로 사용**
    2. 없으면 **기본 스펙 생성** 후 일관 적용

* **금지/주의**

    * 새로운 라이브러리 도입 금지
    * 엔티티 직접 반환 금지
    * Facade 외 계층에 비즈니스 조합 로직 금지
    * N+1 방지 전략 명시(fetch join/entity graph)

* **출력 형식**

    * 파일 경로 + 코드(붙여넣기 가능한 완본)
    * 간단 근거(적용한 규칙 1–3줄)
    * 후속 TODO(마이그레이션/인덱스 등)

---

## 리뷰 체크리스트

* [ ] 공통 응답: **기존 정의 준수** 또는 **기본 스펙 생성** 적용됨
* [ ] 컨트롤러는 검증 + 위임만, 비즈니스 로직 없음
* [ ] Facade에서 작은 컴포넌트 조합/오케스트레이션 수행, 트랜잭션 적절
* [ ] 작은 컴포넌트는 단일 책임/무상태/재사용 가능
* [ ] DTO 사용(엔티티 노출 금지)
* [ ] N+1 방지 확인
* [ ] 테스트는 **BDD 네이밍/구조** 적용, 정상/경계/에러 흐름 포함
* [ ] 로깅 위생(민감정보 금지, 에러 컨텍스트 포함)
* [ ] DB 변경은 마이그레이션 파일 동반

---

## 부록: CBD 구성 예시 스케치

```java
// 작은 컴포넌트(규칙)
@Component
class NameDuplicationPolicy {
  private final SampleRepository repo;
  NameDuplicationPolicy(SampleRepository repo) { this.repo = repo; }
  boolean exists(String normalizedName) { return repo.existsByName(normalizedName); }
}

// 작은 컴포넌트(정규화)
@Component
class NameNormalizer {
  String normalize(String raw) { return raw == null ? null : raw.trim().toLowerCase(); }
}

// Facade(파사드)
@Service
@RequiredArgsConstructor
@Transactional
class SampleFacade {
  private final NameNormalizer normalizer;
  private final NameDuplicationPolicy duplicationPolicy;
  private final SampleRepository repo;
  public SampleDto create(CreateCommand cmd) {
    var name = normalizer.normalize(cmd.name());
    if (duplicationPolicy.exists(name)) throw new DomainConflictException("Name already exists");
    var entity = new SampleEntity(null, name);
    var saved = repo.save(entity);
    return new SampleDto(saved.getId(), saved.getName());
  }
}
```

---

## 공통 응답 details 규약

다음 표준 키를 우선 사용한다. 가능한 한 일관된 스키마를 유지한다.
- fieldErrors: Bean Validation 필드 오류 목록. [{field, reason}]
- constraint: 위반된 제약 식별자(예: UNIQUE_NAME).
- entity: 관련 엔티티/리소스 정보(예: {type:"Bookmark", id:123}).
- correlationId: 요청 상관관계 ID(로그/MDC와 동일).
- hint: 사용자/개발자 힌트 메시지.
- external: 외부 연동 오류 컨텍스트({provider, endpoint, httpStatus, responseSnippet}).

예시:
```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid request",
    "details": {
      "fieldErrors": [{"field":"name","reason":"must not be blank"}],
      "correlationId": "c-20250101-abcdef"
    }
  },
  "timestamp": "2025-01-01T00:00:00Z"
}
```

---

## 로깅/MDC 표준

- 필수 MDC 키: requestId, correlationId, userId, roles, clientIp, userAgent, httpMethod, httpPath, status, latencyMs.
- 민감정보(비밀번호/토큰/주민번호/카드번호 등) 로그 금지. 필요 시 마스킹(앞 2자리만 노출 등).
- 응답 헤더에 requestId, correlationId를 반영해 추적 가능성 확보.
- 서버/로그 시간대: UTC 고정.

---

## API 버저닝·문서화

- 버전 URI: /api/v1 고정. 하위 호환을 우선시하고, 변경 시 v2 신규 도입.
- Deprecated 정책: 응답 헤더 Deprecation, Sunset 활용(가능 시), 마이그레이션 가이드 제공.
- 문서화: 기존 OpenAPI(Springdoc 등) 구성 존재 시 유지·갱신. 신규 라이브러리 도입은 금지 원칙을 준수.

---

## REST 세부 규약 강화

- 페이징 파라미터: page(0-base), size(기본 20, 최대 100), sort=field,dir 다중 허용.
- 페이징 응답: {content, page, size, totalElements, totalPages, sort}를 data에 포함.
- 안정 정렬: 정렬키 동일 시 id ASC 2차 정렬 권장.
- PATCH 정책: 전용 Patch DTO 사용, 제공된 필드만 변경(null은 미사용). 불변 필드는 별도 정책 명시.
- 날짜/시간: UTC, ISO-8601(Z) 고정. 입력/출력/로그 일관.
- Enum 직렬화: name() 사용 고정. enum 변경 시 호환성 체크리스트 필수 수행.
- 멱등성: 생성성 POST는 멱등 키 헤더(Idempotency-Key) 수용 고려.

---

## 외부 연동 어댑터 신뢰성

- 타임아웃: connect/read 2–5s 범위 요구사항에 맞게 설정.
- 재시도: 네트워크 오류/5xx 대상 최대 2회, 지수 백오프(200ms → 400ms → 800ms). 요청 본문이 크거나 비멱등이면 주의.
- 에러 매핑: ErrorCode.EXTERNAL_SERVICE_ERROR로 변환, details.external에 {provider, endpoint, httpStatus, responseSnippet} 포함(민감정보 제외).
- 멱등성: 생성 POST는 키 기반 보호(Idempotency-Key) 적용 검토.
- 구현은 작은 컴포넌트(예: GitLabRetryPolicy)로 캡슐화하고 Facade에서 조합.

---

## JPA 실무 규약 보강

- N+1 방지: fetch join 우선, 필요 시 @EntityGraph 사용. 컬렉션 fetch join + 페이징 금지.
- 낙관적 락: @Version 사용. OptimisticLockException → ErrorCode.CONFLICT로 매핑.
- equals/hashCode: 식별자 기반 구현 권장(영속성 전 상태 주의).
- 연관관계: orphanRemoval/cascade 명확화. 편의 메서드에서 양방향 일관성 유지.
- DB 제약 ↔ Bean Validation 동기화(@NotNull, @Size 등).
- 카운트 쿼리 최적화: 복잡한 페이징 조회 시 countQuery 분리 고려.

---

## 트랜잭션 경계 상세

- Facade public 메서드에 @Transactional. 조회는 readOnly = true.
- 쓰기 흐름 내 읽기 주의(더티 읽기 방지). 필요한 경우 전파(Propagation) 전략 명시.
- 재시도는 멱등 조건에서만. 외부 이벤트 발행은 트랜잭션 커밋 후(outbox 패턴 권장).

---

## 테스트 전략 심화

- Fixture/TestDataBuilder 사용으로 가독성 향상.
- 시간 의존성 제거: Clock 주입/추상화로 테스트 안정화.
- 외부 HTTP 어댑터 테스트: MockRestServiceServer로 성공/타임아웃/5xx/에러 매핑 시나리오 검증.
- 커버리지 목표(권장): 핵심 컴포넌트·파사드 80%+, 라인 70%+, 브랜치 60%+.

---

## 패키지 네이밍 매핑(사이드바 프로젝트)

- com.sidebeam.api: 컨트롤러/Request-Response DTO
- com.sidebeam.application: Facade(Service) 계층(@Transactional)
- com.sidebeam.component: Validator/Mapper/Policy/Enricher 등 작은 컴포넌트
- com.sidebeam.domain: 엔티티/도메인 서비스/리포지토리 포트
- com.sidebeam.infrastructure: JPA 어댑터, 외부 클라이언트, 설정
- GitLab 연동: com.sidebeam.external.gitlab.service, GitLabStorageFileRetriever 등은 infrastructure/adapter로 분류. 포트 인터페이스는 domain 또는 application 경계에 둔다.

---

## 보강된 리뷰 체크리스트(추가)

- [ ] ErrorCode ↔ ErrorResponse 일관성(문자열 코드 직접 사용 금지)
- [ ] 공통 응답 details 규약(fieldErrors/constraint/entity/correlationId/external) 준수
- [ ] MDC 필수 키 적용 및 민감정보 마스킹/헤더 전파
- [ ] API 버저닝·Deprecated 공지·호환성 유지 전략 준수
- [ ] 페이징 기본/최대/안정 정렬 및 응답 스키마 준수
- [ ] 날짜/시간 UTC ISO-8601, Enum name 직렬화
- [ ] 외부 연동 타임아웃/재시도/에러 매핑/멱등성 일관성
- [ ] JPA 낙관적 락/N+1 방지/연관관계 규약 준수
- [ ] 트랜잭션 경계 및 도메인 이벤트(outbox) 정책 준수
- [ ] 테스트: 실패/경계/시간 의존성/외부 어댑터 시나리오 포함, 커버리지 목표 달성
