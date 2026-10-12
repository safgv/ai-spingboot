package org.example.aispingboot.exception;

import lombok.Getter;
import org.example.aispingboot.common.ResultCode;


@Getter
public class BusinessException extends RuntimeException {


    private final String code;

    private final Object data;


    public BusinessException(String message) {

        super(message);

        this.code = ResultCode.BUSINESS_ERROR.getCode();

        this.data = null;
    }


    public BusinessException(String code, String message){

        super(message);

        this.code = code;

        this.data = null;

    }


    public BusinessException(
            String code,
            String message,
            Object data
    ){

        super(message);

        this.code = code;

        this.data = data;

    }

}