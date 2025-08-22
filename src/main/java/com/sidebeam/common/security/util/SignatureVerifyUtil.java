package com.sidebeam.common.security.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * GitLab 웹훅 서명 검증 유틸리티
 * HMAC-SHA256을 사용하여 GitLab 웹훅의 무결성과 인증을 검증합니다.
 */
@Slf4j
public class SignatureVerifyUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final HexFormat HEX_FORMAT = HexFormat.of();

    /**
     * GitLab 웹훅 서명을 검증합니다.
     * 
     * @param payload 웹훅 페이로드 (원본 바이트)
     * @param signature 수신된 서명 값
     * @param secret 웹훅 시크릿 토큰
     * @return 서명이 유효한 경우 true, 그렇지 않으면 false
     */
    public static boolean verifySignature(byte[] payload, String signature, String secret) {
        if (!StringUtils.hasText(secret)) {
            log.warn("Webhook secret is not configured - signature verification skipped");
            return true; // 시크릿이 설정되지 않은 경우 검증 패스
        }

        if (!StringUtils.hasText(signature)) {
            log.warn("Missing webhook signature header");
            return false;
        }

        try {
            String expectedSignature = generateHmacSha256(payload, secret);
            boolean isValid = MessageDigest.isEqual(
                signature.getBytes(StandardCharsets.UTF_8),
                expectedSignature.getBytes(StandardCharsets.UTF_8)
            );
            
            if (!isValid) {
                log.warn("Webhook signature verification failed");
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * 페이로드와 시크릿을 사용하여 HMAC-SHA256 서명을 생성합니다.
     * 
     * @param payload 서명할 데이터
     * @param secret HMAC 키
     * @return 생성된 HMAC-SHA256 서명 (hex 형태)
     */
    private static String generateHmacSha256(byte[] payload, String secret)
            throws NoSuchAlgorithmException, InvalidKeyException {
        
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        Mac mac = Mac.getInstance(HMAC_SHA256);
        mac.init(keySpec);
        
        byte[] signature = mac.doFinal(payload);
        return HEX_FORMAT.formatHex(signature);
    }

    /**
     * 간단한 토큰 기반 검증 (기존 호환성 유지용)
     * 
     * @param providedToken 제공된 토큰
     * @param expectedToken 예상 토큰
     * @return 토큰이 일치하는 경우 true
     */
    public static boolean verifyToken(String providedToken, String expectedToken) {
        if (!StringUtils.hasText(expectedToken)) {
            log.warn("Webhook token is not configured - token verification skipped");
            return true;
        }

        if (!StringUtils.hasText(providedToken)) {
            log.warn("Missing webhook token");
            return false;
        }

        boolean isValid = MessageDigest.isEqual(
            providedToken.getBytes(StandardCharsets.UTF_8),
            expectedToken.getBytes(StandardCharsets.UTF_8)
        );

        if (!isValid) {
            log.warn("Webhook token verification failed");
        }

        return isValid;
    }
}