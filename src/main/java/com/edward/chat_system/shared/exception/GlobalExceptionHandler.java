package com.edward.chat_system.shared.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Null;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.edward.chat_system.shared.dto.ApiResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_CODE_KEY = "value";

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse<Null>> handlingException(Exception exception) {
        ApiResponse<Null> apiResponse = new ApiResponse<>();
        apiResponse.setStatus(ErrorCode.UNCATEGORIZED.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED.getMessage());
        return ResponseEntity.internalServerError().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse<Null>> handlingAppException(AppException exception) {
        ApiResponse<Null> apiResponse = new ApiResponse<>();

        ErrorCode errorCode = exception.getErrorCode();

        apiResponse.setStatus(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    ResponseEntity<ApiResponse<Object>> handlingAccessDeniedException(
            AccessDeniedException exception) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getStatusCode())
                .body(
                        ApiResponse.builder()
                                .status(errorCode.getCode())
                                .message(errorCode.getMessage())
                                .build());
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handlingValidation(
            MethodArgumentNotValidException exception) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED;

        FieldError fieldError = exception.getFieldError();

        if (fieldError != null && fieldError.getDefaultMessage() != null) {
            try {
                String message = fieldError.getDefaultMessage();

                ConstraintViolation<?> violation = fieldError.unwrap(ConstraintViolation.class);

                Map<String, Object> attributes = violation.getConstraintDescriptor().getAttributes();

                errorCode = ErrorCode.valueOf(mapAttribute(message, attributes));

            } catch (Exception e) {
                errorCode = ErrorCode.UNCATEGORIZED;
            }
        }

        ApiResponse<Void> response = new ApiResponse<>();
        response.setStatus(errorCode.getCode());
        response.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }

    private String mapAttribute(String message, Map<String, Object> attr) {
        String value = String.valueOf(attr.get(ERROR_CODE_KEY));
        return message.replace("{" + value + "}", value);
    }
}
