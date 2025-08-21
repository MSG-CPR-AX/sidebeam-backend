package com.sidebeam.testsupport;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 활성 프로파일에 'local'이 포함될 때만 테스트를 실행하도록 활성화하는 애노테이션.
 * ExecutionCondition은 테스트 인스턴스 생성 및 스프링 컨텍스트 초기화 이전에 평가되므로,
 * local 프로파일이 아니면 스프링 부트가 기동되지 않습니다.
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Documented
@ExtendWith(EnabledOnLocalProfileCondition.class)
public @interface EnabledOnLocalProfile {
}
