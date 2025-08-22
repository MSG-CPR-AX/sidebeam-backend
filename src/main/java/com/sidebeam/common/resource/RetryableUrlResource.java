package com.sidebeam.common.resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.UrlResource;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * 외부 HTTP 자원에 접근할 때 일시적인 네트워크 오류에 대한 재시도 로직을 제공하는 UrlResource 확장 클래스
 * Spring의 UrlResource를 상속받아 HTTP 요청 실패 시 지수 백오프 방식으로 재시도를 수행한다.
 * 5xx 서버 오류나 IOException 발생 시에만 재시도하며, 4xx 클라이언트 오류는 즉시 실패 처리한다.
 */
@Slf4j
public class RetryableUrlResource extends UrlResource {

    /**
     * Spring Retry 프레임워크의 재시도 실행을 담당하는 템플릿 객체
     * 지수 백오프 정책과 최대 재시도 횟수를 적용하여 안정적인 재시도 메커니즘을 제공한다.
     */
    private RetryTemplate retryTemplate;

    /**
     * 재시도 최대 횟수 설정값 (기본값: 6회)
     * 네트워크 불안정 상황에서 충분한 재시도 기회를 제공하면서도 무한 대기를 방지한다.
     */
    private static int maxAttempts = 6;

    /**
     * 첫 번째 재시도 전 대기 시간 (기본값: 1000ms)
     * 일시적인 네트워크 오류 해결을 위한 초기 대기 간격을 설정한다.
     */
    private static long initialInterval = 1000;

    /**
     * 재시도 간격 증가 배수 (기본값: 1.1)
     * 지수 백오프 방식으로 재시도 간격을 점진적으로 늘려 서버 부하를 줄인다.
     */
    private static double multiplier = 1.1;

    /**
     * 재시도 간격 최대값 (기본값: 2000ms)
     * 지수 백오프로 인한 과도한 대기 시간을 제한하여 응답성을 보장한다.
     */
    private static long maxInterval = 2000;

    /**
     * 재시도 최대 횟수를 동적으로 변경하는 설정 메서드
     * 애플리케이션 운영 중 네트워크 환경에 따라 재시도 정책을 조정할 수 있다.
     */
    public static void setMaxAttempts(int maxAttempts) {
        RetryableUrlResource.maxAttempts = maxAttempts;
    }

    /**
     * 초기 재시도 대기 간격을 동적으로 변경하는 설정 메서드
     * 네트워크 지연 특성에 맞춰 첫 재시도 타이밍을 최적화할 수 있다.
     */
    public static void setInitialInterval(long initialInterval) {
        RetryableUrlResource.initialInterval = initialInterval;
    }

    /**
     * 재시도 간격 증가 배수를 동적으로 변경하는 설정 메서드
     * 서버 부하 상황에 따라 백오프 강도를 조절하여 시스템 안정성을 확보한다.
     */
    public static void setMultiplier(double multiplier) {
        RetryableUrlResource.multiplier = multiplier;
    }

    /**
     * 재시도 간격 최대값을 동적으로 변경하는 설정 메서드
     * 최대 대기 시간을 제한하여 사용자 경험과 시스템 응답성의 균형을 맞춘다.
     */
    public static void setMaxInterval(long maxInterval) {
        RetryableUrlResource.maxInterval = maxInterval;
    }

    /**
     * URI 객체를 받아 재시도 가능한 URL 리소스를 생성하는 생성자
     * 부모 클래스 초기화 후 재시도 템플릿 설정을 위한 init() 메서드를 호출한다.
     */
    public RetryableUrlResource(URI uri) throws MalformedURLException {
        super(uri);
        init();
    }

    /**
     * URL 객체를 받아 재시도 가능한 URL 리소스를 생성하는 생성자
     * 가장 직접적인 URL 접근 방식으로 재시도 로직이 적용된 리소스를 제공한다.
     */
    public RetryableUrlResource(URL url) {
        super(url);
        init();
    }

    /**
     * 문자열 경로를 받아 재시도 가능한 URL 리소스를 생성하는 생성자
     * 문자열 URL을 파싱하여 유효성 검증 후 재시도 메커니즘을 초기화한다.
     */
    public RetryableUrlResource(String path) throws MalformedURLException {
        super(path);
        init();
    }

    /**
     * 프로토콜과 위치를 분리하여 재시도 가능한 URL 리소스를 생성하는 생성자
     * URL 구성 요소를 개별적으로 받아 보다 세밀한 URL 제어가 가능하다.
     */
    public RetryableUrlResource(String protocol, String location) throws MalformedURLException {
        super(protocol, location);
        init();
    }

    /**
     * 프로토콜, 위치, 프래그먼트를 모두 지정하여 재시도 가능한 URL 리소스를 생성하는 생성자
     * URL의 모든 구성 요소를 세밀하게 제어하면서 재시도 기능을 제공한다.
     */
    public RetryableUrlResource(String protocol, String location, String fragment) throws MalformedURLException {
        super(protocol, location, fragment);
        init();
    }

    /**
     * 재시도 템플릿을 초기화하는 내부 메서드
     * 지수 백오프 정책을 설정하고 RetryIOException에 대해서만 재시도하도록 구성한다.
     * 모든 생성자에서 공통으로 호출되어 일관된 재시도 동작을 보장한다.
     */
    private void init() {
        // 지수 백오프 정책 객체 생성 및 설정
        ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(initialInterval);  // 첫 재시도 대기 시간 설정
        policy.setMultiplier(multiplier);            // 재시도 간격 증가 배수 설정
        policy.setMaxInterval(maxInterval);          // 최대 재시도 간격 제한 설정

        // RetryTemplate 빌더 패턴으로 재시도 정책 구성
        this.retryTemplate = RetryTemplate.builder()
                .retryOn(RetryIOException.class)      // RetryIOException 발생 시에만 재시도
                .maxAttempts(maxAttempts)            // 최대 재시도 횟수 설정
                .customBackoff(policy)               // 커스텀 백오프 정책 적용
                .build();
    }

    /**
     * InputStream을 얻기 위한 핵심 메서드로 부모 클래스의 getInputStream()을 재정의
     * HTTP 프로토콜인 경우 재시도 로직을 적용하고, 그 외 프로토콜은 부모 클래스 동작을 그대로 사용한다.
     * 재시도 템플릿을 통해 네트워크 오류 발생 시 자동으로 재시도를 수행하여 안정성을 확보한다.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        URL url = getURL();
        // HTTP/HTTPS 프로토콜인지 확인하여 재시도 로직 적용 여부 결정
        if (url.getProtocol().startsWith("http")) {
            try {
                // RetryTemplate을 사용하여 재시도 가능한 HTTP 요청 실행
                return retryTemplate.execute(context -> getHttpUrlInputStream(url));
            } catch (Exception exception) {
                log.error("Http URL Resource error", exception);
                throw exception;
            }
        } else {
            // HTTP가 아닌 프로토콜(file, ftp 등)은 부모 클래스의 기본 동작 사용
            return super.getInputStream();
        }
    }

    /**
     * HTTP URL에 대한 실제 연결 수행 및 응답 처리를 담당하는 핵심 로직 메서드
     * HTTP 상태 코드에 따라 재시도 여부를 결정하고, 적절한 예외를 발생시켜 재시도 메커니즘을 제어한다.
     * 5xx 서버 오류와 IOException은 재시도 대상으로, 4xx 클라이언트 오류는 즉시 실패 처리한다.
     */
    private InputStream getHttpUrlInputStream(URL url) throws IOException {
        HttpURLConnection httpCon = null;
        try {
            // URL 연결 객체 생성 및 HTTP 연결로 캐스팅
            URLConnection con = url.openConnection();
            ResourceUtils.useCachesIfNecessary(con);  // Spring의 캐시 정책 적용
            httpCon = (HttpURLConnection) con;

            // HTTP 응답 상태 코드 확인
            int code = httpCon.getResponseCode();

            if (code >= 500) {
                // 5xx 서버 오류: 재시도 대상으로 RetryIOException 발생
                httpCon.disconnect();
                log.error("Resource Not Found {}, Http Status={}", url, code);
                throw new RetryIOException(String.format("Resource IOException %s, Http Status=%d", url, code));
            } else if (code >= 200 && code < 300) {
                // 2xx 성공: 정상적으로 InputStream 반환
                return httpCon.getInputStream();
            } else {
                // 4xx 클라이언트 오류: 재시도하지 않고 즉시 실패 처리
                log.error("Resource Not Found {}, Http Status={}", url, code);
                throw new FileNotFoundException(String.format("Resource Not Found %s, Http Status=%s", url, code));
            }

        } catch (FileNotFoundException fileNotFoundException) {
            // FileNotFoundException: 재시도하지 않는 예외로 그대로 전파
            if (httpCon != null) httpCon.disconnect();
            throw fileNotFoundException;
        } catch (IOException ioException) {
            // IOException: 네트워크 오류로 간주하여 재시도 대상으로 변환
            log.error("Resource {} Read Error {}", url.toString(), ioException.getMessage());
            if (httpCon != null) httpCon.disconnect();
            throw new RetryIOException(ioException);
        } catch (Throwable throwable) {
            // 기타 예외: 재시도하지 않고 그대로 전파
            log.error("Resource {} Read Error {}", url.toString(), throwable.getMessage());
            if (httpCon != null) httpCon.disconnect();
            throw throwable;
        }
    }

    /**
     * 재시도 가능한 IOException을 나타내는 커스텀 예외 클래스
     * RetryTemplate이 이 예외를 감지했을 때만 재시도를 수행하도록 하는 마커 역할을 한다.
     * 일반 IOException과 구분하여 재시도 로직의 정확한 제어를 가능하게 한다.
     */
    public static class RetryIOException extends IOException {
        /**
         * 기본 생성자 - 메시지 없이 예외 객체만 생성
         */
        public RetryIOException() {}

        /**
         * 메시지를 포함한 예외 생성자 - 오류 상황에 대한 설명 제공
         */
        public RetryIOException(String message) {super(message);}

        /**
         * 메시지와 원인 예외를 모두 포함한 생성자 - 예외 체이닝을 통한 상세한 오류 추적
         */
        public RetryIOException(String message, Throwable cause) {super(message, cause);}

        /**
         * 원인 예외만 포함한 생성자 - 기존 예외를 재시도 가능한 예외로 래핑
         */
        public RetryIOException(Throwable cause) {super(cause);}
    }
}
