package com.sidebeam.common.exception;

import com.sidebeam.common.core.exception.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ExceptionHierarchyTest용 테스트 컨트롤러
 */
@RestController
public class ExceptionHierarchyTestController {

    @GetMapping("/test/technical-exception")
    public String testTechnicalException() {
        throw new TechnicalException(ErrorCode.PROPERTY_CONVERSION_ERROR);
    }

    @GetMapping("/test/domain-exception")
    public String testDomainException() {
        throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
    }

    @GetMapping("/test/validation-exception")
    public String testValidationException() {
        throw new ValidationException(ErrorCode.INVALID_PARAMETER_FORMAT);
    }

    @GetMapping("/test/technical-exception-custom")
    public String testTechnicalExceptionCustom() {
        throw new TechnicalException(ErrorCode.DATA_PARSING_ERROR, "Custom parsing error message");
    }

    @GetMapping("/test/domain-exception-business-rule")
    public String testDomainExceptionBusinessRule() {
        throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION);
    }

    @GetMapping("/test/validation-exception-range")
    public String testValidationExceptionRange() {
        throw new ValidationException(ErrorCode.PARAMETER_OUT_OF_RANGE);
    }

    @GetMapping("/test/business-exception-compatibility")
    public String testBusinessExceptionCompatibility() {
        // 기존 방식으로 BusinessException을 사용하려고 하면 컴파일 에러가 발생해야 함
        // 대신 구체적인 하위 클래스를 사용해야 함
        throw new TechnicalException("Legacy compatibility test");
    }
}