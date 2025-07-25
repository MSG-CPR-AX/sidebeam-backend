package com.sidebeam.common.exception;

import com.sidebeam.common.core.exception.BusinessException;
import com.sidebeam.common.core.exception.ErrorCode;
import com.sidebeam.common.core.exception.SystemException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 테스트용 컨트롤러 (별도 클래스)
 */
@RestController
public class TestExceptionController {

    @GetMapping("/test/business-exception")
    public String testBusinessException() {
        throw new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND);
    }

    @GetMapping("/test/system-exception")
    public String testSystemException() {
        throw new SystemException(ErrorCode.GITLAB_CONNECTION_ERROR);
    }

    @GetMapping("/test/illegal-argument")
    public String testIllegalArgumentException() {
        throw new IllegalArgumentException("Invalid argument provided");
    }

    @GetMapping("/test/generic-exception")
    public String testGenericException() {
        throw new RuntimeException("Generic runtime exception");
    }

    @GetMapping("/test/missing-param")
    public String testMissingParameter(@RequestParam String requiredParam) {
        return "Success";
    }
}