package com.sidebeam.common.jasypt;

import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Utility test to generate ENC(...) using the same encryptor as runtime.
 * Usage:
 *   export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'
 *   ./gradlew test \
 *     -Dspring.profiles.active=local \
 *     -Djasypt.value='plain-text' \
 *     --tests '*JasyptEncryptorTest.printEncrypted'
 */
@ActiveProfiles("local")
@SpringBootTest
class JasyptEncryptorTest {

    @Autowired
    StringEncryptor encryptor;

    @Test
    void printEncrypted() {
        // Skip unless explicitly invoked with -Djasypt.value and some password present
        String value = System.getProperty("jasypt.value");
        Assumptions.assumeTrue(value != null && !value.isBlank(), "Provide -Djasypt.value=<PLAIN_TEXT>");

        String pw = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        String pwProp = System.getProperty("jasypt.encryptor.password");
        Assumptions.assumeTrue((pw != null && !pw.isBlank()) || (pwProp != null && !pwProp.isBlank()),
                "Set JASYPT_ENCRYPTOR_PASSWORD or -Djasypt.encryptor.password");

        String enc = encryptor.encrypt(value);
        System.out.println("ENC(" + enc + ")");
    }
}
