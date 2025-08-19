package com.sidebeam.config.security;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Jasypt를 사용하여 평문을 암호화하는 유틸리티 테스트
 * 
 * <p>이 테스트는 개발자가 application.yml에 사용할 암호화된 값을 생성하는 데 사용됩니다.</p>
 * 
 * <h3>사용법:</h3>
 * <pre>
 * # 1. 환경변수 설정
 * export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
 * 
 * # 2. 암호화할 값 지정하여 테스트 실행
 * ./gradlew test \
 *   -Dspring.profiles.active=local \
 *   -Djasypt.value='your-plain-text-value' \
 *   --tests '*JasyptEncryptorTest.printEncrypted'
 * 
 * # 3. 테스트 로그에서 ENC(...) 값 확인 후 application.yml에 적용
 * </pre>
 */
@Slf4j
@EnabledIfEnvironmentVariable(named = "JASYPT_ENCRYPTOR_PASSWORD", matches = ".+")
@SpringBootTest
@ActiveProfiles("local")
class JasyptEncryptorTest {
    
    @Autowired
    private StringEncryptor stringEncryptor;
    
    /**
     * 시스템 프로퍼티로 전달받은 평문을 암호화하여 출력합니다.
     * 
     * <p>사용법: -Djasypt.value='암호화할 평문' 시스템 프로퍼티로 전달</p>
     */
    @Test
    void printEncrypted() {
        String plainValue = System.getProperty("jasypt.value");
        assertNotNull(plainValue, 
            "암호화할 값을 시스템 프로퍼티로 전달해주세요: -Djasypt.value='your-plain-text-value'");
        
        String encryptedValue = stringEncryptor.encrypt(plainValue);
        
        log.info("");
        log.info("===============================================================");
        log.info("🔐 Jasypt 암호화 결과");
        log.info("===============================================================");
        log.info("평문: {}", plainValue);
        log.info("암호문: ENC({})", encryptedValue);
        log.info("===============================================================");
        log.info("위 암호문을 application.yml에 복사하여 사용하세요.");
        log.info("===============================================================");
        log.info("");
    }
    
    /**
     * 복호화 테스트 - 암호화된 값이 정상적으로 복호화되는지 확인
     */
    @Test
    void testDecryption() {
        String plainValue = "test-value-for-decryption";
        String encryptedValue = stringEncryptor.encrypt(plainValue);
        String decryptedValue = stringEncryptor.decrypt(encryptedValue);
        
        assertNotNull(encryptedValue);
        assertNotNull(decryptedValue);
        
        log.info("✅ 암호화/복호화 테스트 성공:");
        log.info("원본: {}", plainValue);
        log.info("암호문: {}", encryptedValue);
        log.info("복호문: {}", decryptedValue);
        
        // 원본과 복호화된 값이 동일한지 확인
        assert plainValue.equals(decryptedValue) : "복호화된 값이 원본과 다릅니다!";
    }
    
    /**
     * 여러 값을 한번에 암호화하는 편의 메서드 (개발용)
     * 
     * <p>이 테스트는 실제 값들을 하드코딩하여 실행할 때만 사용하세요.</p>
     */
    @Test
    void encryptMultipleValues() {
        // 데모용 샘플 값들 - 실제 사용 시 실제 값으로 교체
        String[] valuesToEncrypt = {
            "sample-gitlab-access-token-for-demo",
            "12345",
            "sample-webhook-secret-token-for-demo"
        };
        
        if (valuesToEncrypt == null 
            || valuesToEncrypt.length == 0 
            || java.util.Arrays.stream(valuesToEncrypt).anyMatch(java.util.Objects::isNull)) {
            log.warn("⚠️  암호화할 값들을 하드코딩한 후 테스트를 실행하세요.");
            return;
        }

        log.info("");
        log.info("===============================================================");
        log.info("🔐 여러 값 암호화 결과");
        log.info("===============================================================");

        java.util.concurrent.atomic.AtomicInteger index = new java.util.concurrent.atomic.AtomicInteger(1);
        for (String plainValue : valuesToEncrypt) {
            String encryptedValue = stringEncryptor.encrypt(plainValue);
            int i = index.getAndIncrement();
            log.info("{}. 평문: {}", i, plainValue);
            log.info("   암호문: ENC({})", encryptedValue);
            log.info("");
        }

        log.info("===============================================================");
    }
}