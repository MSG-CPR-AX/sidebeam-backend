package com.sidebeam.common.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Local profile only guard: ensures JASYPT_ENCRYPTOR_PASSWORD is provided.
 * Fails fast with a friendly message to prevent starting without the master key.
 */
@Slf4j
@Profile("local")
@Component
public class LocalJasyptGuard implements ApplicationRunner {

    private final Environment env;

    public LocalJasyptGuard(Environment env) {
        this.env = env;
    }

    @Override
    public void run(ApplicationArguments args) {
        String password = env.getProperty("jasypt.encryptor.password");
        if (password == null || password.isBlank()) {
            log.error("[LOCAL] JASYPT_ENCRYPTOR_PASSWORD is not set.\n" +
                    "Please export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key' and re-run.\n" +
                    "Example:\n  export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'\n  ./gradlew bootRun --args='--spring.profiles.active=local'");
            System.exit(1);
        }
    }
}
