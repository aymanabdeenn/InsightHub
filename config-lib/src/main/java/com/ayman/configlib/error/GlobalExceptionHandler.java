package com.ayman.configlib.error;

import com.ayman.configlib.response.ResponseCodeProperties;
import com.ayman.configlib.response.GenericResponseDTO;
import com.ayman.configlib.observability.correlation.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ResponseCodeProperties responseCodeProperties;
    private final MessageSource messageSource;

    @Autowired
    public GlobalExceptionHandler(@Qualifier("responseCodeProperties") ResponseCodeProperties responseCodeProperties, MessageSource messageSource) {
        this.responseCodeProperties = responseCodeProperties;
        this.messageSource = messageSource;
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<GenericResponseDTO<Object>> handleApiException(ApiException ex, Locale locale) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);

        String errorCode = ex.getErrorCode();
        ResponseCodeProperties.ResponseDetail detail = responseCodeProperties.getCodes().get(errorCode);
        if (detail == null) {
            log.warn("Unknown error code '{}' thrown, falling back to INTERNAL_ERROR", errorCode);
            errorCode = "INTERNAL_ERROR";
            detail = responseCodeProperties.getCodes().get(errorCode);
        }

        String message = messageSource.getMessage(errorCode, null, locale);

        log.error("API exception [{}]: {}", errorCode, message, ex);

        GenericResponseDTO<Object> body = new GenericResponseDTO<>(
                correlationId, errorCode, message, ex.getMessage(),(long) detail.getStatus(), null
        );

        return ResponseEntity.status(detail.getStatus()).body(body);
    }

    @ExceptionHandler(NoSuchMessageException.class)
    public ResponseEntity<GenericResponseDTO<Object>> handleMissingTranslation(NoSuchMessageException ex) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);

        String code = "INTERNAL_ERROR";
        ResponseCodeProperties.ResponseDetail fallback = responseCodeProperties.getCodes().get(code);
        log.error("Missing translation key for an error code — check your .properties files", ex);

        GenericResponseDTO<Object> body = new GenericResponseDTO<>(
                correlationId,
                code,
                "Internal server error",
                ex.getMessage(),
                (long) fallback.getStatus(),
                null
        );
        return ResponseEntity.status(fallback.getStatus()).body(body);
    }

    @ExceptionHandler(DataSourceConnectionException.class)
    public ResponseEntity<GenericResponseDTO<Object>> handleDataSourceConnectionException(DataSourceConnectionException ex, Locale locale) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);

        String errorCode = ex.getErrorCode();
        ResponseCodeProperties.ResponseDetail detail = responseCodeProperties.getCodes().get(errorCode);
        if (detail == null) {
            log.warn("Unknown error code '{}' thrown, falling back to INTERNAL_ERROR", errorCode);
            errorCode = "INTERNAL_ERROR";
            detail = responseCodeProperties.getCodes().get(errorCode);
        }

        String message = messageSource.getMessage(errorCode, null, locale);

        log.error("DataSourceConnection exception [{}]: {}", errorCode, message, ex);

        GenericResponseDTO<Object> body = new GenericResponseDTO<>(
                correlationId, errorCode, message, ex.getDeveloperMessage() + " \n\n " + ex.getFailureReason(),(long) detail.getStatus(), null
        );

        return ResponseEntity.status(detail.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponseDTO<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException ex, Locale locale) {
        Map<String, String> errors = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);

        String code = "INVALID_INPUT";
        String message = messageSource.getMessage(code,null, locale);
        GenericResponseDTO<Map<String, String>> response = new GenericResponseDTO<>(
                correlationId,
                code,
                message,
                "One or more fields failed validation checks.",
                (long) HttpStatus.BAD_REQUEST.value(),
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

}
