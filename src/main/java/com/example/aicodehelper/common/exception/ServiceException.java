package com.example.aicodehelper.common.exception;

import com.example.aicodehelper.common.constant.Constants;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private final Integer code;

    public ServiceException(String message) {
        super(message);
        this.code = Constants.FAIL;
    }

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
