package com.sidebeam.common.security.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Local 프로파일에서 Jasypt 암호화 키가 설정되지 않은 경우 애플리케이션을 종료하는 가드 컴포넌트
 * 
 * <p>이 컴포넌트는 local 프로파일에서만 활성화되며, JASYPT_ENCRYPTOR_PASSWORD 환경변수가
 * 설정되지 않은 경우 친절한 에러 메시지와 함께 애플리케이션을 즉시 종료합니다.</p>
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalJasyptGuard implements ApplicationRunner {
    
    private final Environment environment;
    
    @Override
    public void run(ApplicationArguments args) {
        String jasyptPassword = environment.getProperty("jasypt.encryptor.password");
        
        if (jasyptPassword == null || jasyptPassword.isBlank()) {
            log.error("===============================================================");
            log.error("❌ [LOCAL] JASYPT_ENCRYPTOR_PASSWORD 환경변수가 설정되지 않았습니다!");
            log.error("===============================================================");
            log.error("");
            log.error("로컬 개발환경에서 암호화된 설정값을 사용하려면 다음과 같이 설정하세요:");
            log.error("");
            log.error("1. 환경변수 설정:");
            log.error("   export JASYPT_ENCRYPTOR_PASSWORD='your-local-master-key'");
            log.error("");
            log.error("2. 애플리케이션 재실행:");
            log.error("   ./gradlew bootRun");
            log.error("");
            log.error("또는 IDE에서 실행 시 Environment Variables에 추가:");
            log.error("   JASYPT_ENCRYPTOR_PASSWORD=your-local-master-key");
            log.error("");
            log.error("===============================================================");
            
            System.exit(1);
        }
        
        log.info("✅ [LOCAL] Jasypt 암호화 키가 정상적으로 설정되었습니다.");
    }
}