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
  public static ErrorResponse of(String code, String message, Map<String, Object> details) {
    return new ErrorResponse(code, message, details);
  }
}

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Invalid request"),
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "Entity not found"),
  CONFLICT(HttpStatus.CONFLICT, "Conflicting state"),
  BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated");
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
    var err = ErrorResponse.of("VALIDATION_ERROR", "Invalid request", Map.of("fieldErrors", fieldErrors));
    return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus()).body(ApiResponse.error(err));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  ResponseEntity<ApiResponse<Void>> handleNotFound(EntityNotFoundException ex) {
    var err = ErrorResponse.of("ENTITY_NOT_FOUND", ex.getMessage(), Map.of());
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