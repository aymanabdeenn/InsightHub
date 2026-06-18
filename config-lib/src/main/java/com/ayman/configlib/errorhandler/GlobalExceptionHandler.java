package com.ayman.configlib.errorhandler;

import com.ayman.configlib.errorhandler.dto.GenericResponseDTO;
import com.ayman.configlib.exception.GeneralAppException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(GeneralAppException.class)
    public GenericResponseDTO<?> handleGeneralException(GeneralAppException ex) {
        return ex.getResponse();
    }
}
