package com.sidebeam.config.security;

import com.sidebeam.testsupport.EnabledOnLocalProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Jasypt 암호화된 속성값이 올바르게 복호화되어 주입되는지 검증하는 테스트
 *
 * <p>이 테스트는 local 프로파일에서만 실행되며, application.yml의 ENC(...) 값들이
 * 정상적으로 복호화되어 Spring의 @Value로 주입되는지 확인합니다.</p>
 *
 * <h3>실행 전 준비사항:</h3>
 * <ul>
 *   <li>JASYPT_ENCRYPTOR_PASSWORD 환경변수 설정</li>
 *   <li>application.yml의 ENC(...) 값들이 실제 암호화된 값으로 교체되어 있어야 함</li>
 * </ul>
 */
@SpringBootTest
@EnabledOnLocalProfile
class JasyptLocalProfileTest {

    @Value("${gitlab.access-token}")
    private String gitlabAccessToken;

    @Value("${gitlab.root-group-id}")
    private String gitlabRootGroupId;

    @Value("${webhook.secret-token}")
    private String webhookSecretToken;

    /**
     * GitLab 액세스 토큰이 복호화되어 주입되는지 검증
     */
    @Test
    void givenEncryptedGitlabAccessToken_whenInjected_thenDecryptedValueReceived() {
        // Given & When: Spring이 ENC(...) 값을 복호화하여 주입

        // Then: 복호화된 평문이 주입되어야 함
        assertNotNull(gitlabAccessToken, "GitLab 액세스 토큰이 주입되지 않았습니다.");
        assertFalse(gitlabAccessToken.startsWith("ENC("),
            "GitLab 액세스 토큰이 암호화된 상태로 주입되었습니다. 복호화가 실행되지 않았습니다.");
        assertFalse(gitlabAccessToken.contains("REPLACE_WITH_ENCRYPTED"),
            "GitLab 액세스 토큰이 플레이스홀더 상태입니다. 실제 암호화된 값으로 교체해주세요.");

        System.out.println("✅ GitLab 액세스 토큰 복호화 성공: " + maskSensitiveValue(gitlabAccessToken));
    }

    /**
     * GitLab 루트 그룹 ID가 복호화되어 주입되는지 검증
     */
    @Test
    void givenEncryptedGitlabRootGroupId_whenInjected_thenDecryptedValueReceived() {
        // Given & When: Spring이 ENC(...) 값을 복호화하여 주입

        // Then: 복호화된 평문이 주입되어야 함
        assertNotNull(gitlabRootGroupId, "GitLab 루트 그룹 ID가 주입되지 않았습니다.");
        assertFalse(gitlabRootGroupId.startsWith("ENC("),
            "GitLab 루트 그룹 ID가 암호화된 상태로 주입되었습니다. 복호화가 실행되지 않았습니다.");
        assertFalse(gitlabRootGroupId.contains("REPLACE_WITH_ENCRYPTED"),
            "GitLab 루트 그룹 ID가 플레이스홀더 상태입니다. 실제 암호화된 값으로 교체해주세요.");

        System.out.println("✅ GitLab 루트 그룹 ID 복호화 성공: " + gitlabRootGroupId);
    }

    /**
     * 웹훅 시크릿 토큰이 복호화되어 주입되는지 검증
     */
    @Test
    void givenEncryptedWebhookSecretToken_whenInjected_thenDecryptedValueReceived() {
        // Given & When: Spring이 ENC(...) 값을 복호화하여 주입

        // Then: 복호화된 평문이 주입되어야 함
        assertNotNull(webhookSecretToken, "웹훅 시크릿 토큰이 주입되지 않았습니다.");
        assertFalse(webhookSecretToken.startsWith("ENC("),
            "웹훅 시크릿 토큰이 암호화된 상태로 주입되었습니다. 복호화가 실행되지 않았습니다.");
        assertFalse(webhookSecretToken.contains("REPLACE_WITH_ENCRYPTED"),
            "웹훅 시크릿 토큰이 플레이스홀더 상태입니다. 실제 암호화된 값으로 교체해주세요.");

        System.out.println("✅ 웹훅 시크릿 토큰 복호화 성공: " + maskSensitiveValue(webhookSecretToken));
    }

    /**
     * 모든 암호화된 속성값들이 정상적으로 복호화되는지 통합 검증
     */
    @Test
    void givenAllEncryptedProperties_whenApplicationStarts_thenAllValuesDecrypted() {
        // Given & When: 애플리케이션 시작 시 모든 암호화된 값들이 복호화됨

        // Then: 모든 값들이 복호화된 상태여야 함
        assertAll("모든 암호화된 속성값 복호화 검증",
            () -> assertNotNull(gitlabAccessToken, "GitLab 액세스 토큰 누락"),
            () -> assertNotNull(gitlabRootGroupId, "GitLab 루트 그룹 ID 누락"),
            () -> assertNotNull(webhookSecretToken, "웹훅 시크릿 토큰 누락"),
            () -> assertFalse(gitlabAccessToken.startsWith("ENC("), "GitLab 액세스 토큰 복호화 실패"),
            () -> assertFalse(gitlabRootGroupId.startsWith("ENC("), "GitLab 루트 그룹 ID 복호화 실패"),
            () -> assertFalse(webhookSecretToken.startsWith("ENC("), "웹훅 시크릿 토큰 복호화 실패")
        );

        System.out.println("===============================================================");
        System.out.println("✅ 모든 암호화된 속성값 복호화 성공!");
        System.out.println("===============================================================");
        System.out.println("GitLab 액세스 토큰: " + maskSensitiveValue(gitlabAccessToken));
        System.out.println("GitLab 루트 그룹 ID: " + gitlabRootGroupId);
        System.out.println("웹훅 시크릿 토큰: " + maskSensitiveValue(webhookSecretToken));
        System.out.println("===============================================================");
    }

    /**
     * 민감한 값을 마스킹하여 로그에 안전하게 출력
     *
     * @param value 마스킹할 값
     * @return 마스킹된 값
     */
    private String maskSensitiveValue(String value) {
        if (value == null || value.length() <= 8) {
            return "***";
        }
        return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
    }
}