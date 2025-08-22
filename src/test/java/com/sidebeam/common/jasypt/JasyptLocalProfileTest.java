package com.sidebeam.common.jasypt;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("local")
@TestPropertySource(properties = {
        "jasypt.encryptor.password=test-master-key"
})
@SpringBootTest
class JasyptLocalProfileTest {

    private static String encrypted;

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        // Configure an encryptor matching starter defaults
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("test-master-key");
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        // Optional typical defaults
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");

        PooledPBEStringEncryptor enc = new PooledPBEStringEncryptor();
        enc.setConfig(config);

        encrypted = enc.encrypt("super-secret-api-key");
        registry.add("my.external.api-key", () -> "ENC(" + encrypted + ")");
    }

    @Value("${my.external.api-key}")
    String apiKey;

    @Test
    void decryptedInjected() {
        assertNotNull(apiKey);
        assertFalse(apiKey.startsWith("ENC("), "Should be decrypted, not the raw ENC(...) value");
        assertEquals("super-secret-api-key", apiKey);
    }
}
