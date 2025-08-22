package com.sidebeam.common.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * Logback converter for masking sensitive information in log messages.
 * 민감한 정보(비밀번호, 토큰, 카드번호, 주민번호 등)를 로그에서 마스킹합니다.
 */
public class SensitiveDataMaskingConverter extends ClassicConverter {

    // 민감 정보 패턴들 (대소문자 구분 없음)
    private static final Pattern[] SENSITIVE_PATTERNS = {
            // 비밀번호 관련
            Pattern.compile("(?i)(password|pwd|passwd)([\\s=:]+)([\\S]+)", Pattern.CASE_INSENSITIVE),
            
            // 토큰/키 관련
            Pattern.compile("(?i)(token|key|secret|authorization|bearer)([\\s=:]+)([\\S]+)", Pattern.CASE_INSENSITIVE),
            
            // API 키 패턴
            Pattern.compile("(?i)(api[_-]?key|access[_-]?token)([\\s=:]+)([\\S]+)", Pattern.CASE_INSENSITIVE),
            
            // JWT 토큰 패턴 (eyJ로 시작하는 토큰)
            Pattern.compile("(eyJ[A-Za-z0-9+/=]+\\.[A-Za-z0-9+/=]+\\.[A-Za-z0-9+/=]*)", Pattern.CASE_INSENSITIVE),
            
            // 신용카드 번호 (4자리씩 4그룹 또는 16자리 연속)
            Pattern.compile("\\b(?:\\d{4}[-\\s]?){3}\\d{4}\\b"),
            Pattern.compile("\\b\\d{16}\\b"),
            
            // 주민등록번호 (6자리-7자리)
            Pattern.compile("\\b\\d{6}-\\d{7}\\b"),
            
            // 이메일 주소 (부분 마스킹)
            Pattern.compile("([a-zA-Z0-9._%+-]+)(@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"),
            
            // 전화번호 (한국 형식)
            Pattern.compile("\\b0\\d{1,2}-\\d{3,4}-\\d{4}\\b"),
            Pattern.compile("\\b\\d{3}-\\d{4}-\\d{4}\\b")
    };

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        return maskSensitiveData(message);
    }

    /**
     * 민감한 데이터를 마스킹합니다.
     * 
     * @param input 원본 메시지
     * @return 마스킹된 메시지
     */
    public static String maskSensitiveData(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }

        String result = input;

        // 비밀번호, 토큰, 키 등 마스킹
        result = SENSITIVE_PATTERNS[0].matcher(result).replaceAll("$1$2***MASKED***");
        result = SENSITIVE_PATTERNS[1].matcher(result).replaceAll("$1$2***MASKED***");
        result = SENSITIVE_PATTERNS[2].matcher(result).replaceAll("$1$2***MASKED***");
        
        // JWT 토큰 마스킹 (앞 10자리만 보여주고 나머지 마스킹)
        result = SENSITIVE_PATTERNS[3].matcher(result).replaceAll(matchResult -> {
            String token = matchResult.group(1);
            if (token.length() > 10) {
                return token.substring(0, 10) + "***MASKED***";
            }
            return "***MASKED***";
        });

        // 신용카드 번호 마스킹 (앞 4자리, 뒤 4자리만 보여줌)
        result = SENSITIVE_PATTERNS[4].matcher(result).replaceAll(matchResult -> {
            String card = matchResult.group().replaceAll("[-\\s]", "");
            if (card.length() == 16) {
                return card.substring(0, 4) + "****-****-" + card.substring(12);
            }
            return "****-****-****-****";
        });
        
        result = SENSITIVE_PATTERNS[5].matcher(result).replaceAll(matchResult -> {
            String card = matchResult.group();
            return card.substring(0, 4) + "********" + card.substring(12);
        });

        // 주민등록번호 마스킹 (앞 6자리만 보여줌)
        result = SENSITIVE_PATTERNS[6].matcher(result).replaceAll("$1-*******");

        // 이메일 마스킹 (사용자명의 처음 2자리와 도메인만 보여줌)
        result = SENSITIVE_PATTERNS[7].matcher(result).replaceAll(matchResult -> {
            String username = matchResult.group(1);
            String domain = matchResult.group(2);
            if (username.length() > 2) {
                return username.substring(0, 2) + "***" + domain;
            }
            return "***" + domain;
        });

        // 전화번호 마스킹 (뒤 4자리만 보여줌)
        result = SENSITIVE_PATTERNS[8].matcher(result).replaceAll(matchResult -> {
            String phone = matchResult.group();
            String[] parts = phone.split("-");
            if (parts.length == 3) {
                return "***-***-" + parts[2];
            }
            return "***-***-****";
        });
        
        result = SENSITIVE_PATTERNS[9].matcher(result).replaceAll(matchResult -> {
            String phone = matchResult.group();
            String[] parts = phone.split("-");
            if (parts.length == 3) {
                return "***-***-" + parts[2];
            }
            return "***-***-****";
        });

        return result;
    }
}