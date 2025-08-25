plugins {
    id("java")
    id("org.springframework.boot") version "3.5.4"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "com.sidebeam"
version = "1.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_24

repositories {
    mavenCentral()
}

// 공통 버전 상수 추출
val jasyptVersion = "3.0.5"
val resilience4jBomVersion = "2.3.0"
val gitlab4jVersion = "6.0.0-rc.10"
val springdocVersion = "2.8.9"
val mapstructVersion = "1.6.3"
val jsonOrgVersion = "20250517"
val archunitVersion = "1.4.1"
val nettyDnsNativeVersion = "4.2.4.Final"
val jacksonBomVersion = "2.17.2"
val jaxrsVersion = "4.0.0"
val jsonSchemaVersion = "1.5.1"
val junitBomVersion = "5.13.4"
val logbackVersion = "8.0"

dependencies {
    // Implementation
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Core/Infra
    implementation("org.springframework.retry:spring-retry")
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:$jasyptVersion")

    // Caffeine Cache
    implementation("com.github.ben-manes.caffeine:caffeine")

    // Resilience4j
    implementation(platform("io.github.resilience4j:resilience4j-bom:$resilience4jBomVersion"))
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    implementation("io.github.resilience4j:resilience4j-reactor")

    // Jackson / YAML
    implementation(platform("com.fasterxml.jackson:jackson-bom:$jacksonBomVersion"))
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // GitLab / JAX-RS
    implementation("org.gitlab4j:gitlab4j-api:$gitlab4jVersion")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:$jaxrsVersion")

    // JSON Schema Validation
    implementation("org.everit.json:org.everit.json.schema:$jsonSchemaVersion")
    implementation("org.json:json:$jsonOrgVersion")

    // Mapstruct
    implementation("org.mapstruct:mapstruct:$mapstructVersion")

    // API Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

    // Structured Logging
    implementation("net.logstash.logback:logstash-logback-encoder:$logbackVersion")

    // CompileOnly
    compileOnly("org.projectlombok:lombok")

    // Annotation Processor
    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.mapstruct:mapstruct-processor:$mapstructVersion")

    // RuntimeOnly
    // macOS ARM64 전용 DNS 리졸버 (로컬 개발환경용)
    if (System.getProperty("os.name").lowercase().startsWith("mac") &&
        System.getProperty("os.arch") == "aarch64" &&
        !System.getenv("CI").toBoolean()) { // CI 환경이 아닌 경우에만
        runtimeOnly("io.netty:netty-resolver-dns-native-macos:$nettyDnsNativeVersion:osx-aarch_64")
    }

    // TestImplementation
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.tngtech.archunit:archunit-junit5:$archunitVersion")

    // TestCompileOnly
    testCompileOnly("org.projectlombok:lombok")

    // TestAnnotationProcessor
    testAnnotationProcessor("org.projectlombok:lombok")
}

configurations.configureEach {
    exclude(group = "commons-logging", module = "commons-logging")
}

tasks.test {
    useJUnitPlatform()
    // Pass system properties to test JVM
    systemProperties(System.getProperties().toMap() as Map<String, Any>)
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% line coverage
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.70".toBigDecimal() // 70% branch coverage
            }
        }
    }
}
