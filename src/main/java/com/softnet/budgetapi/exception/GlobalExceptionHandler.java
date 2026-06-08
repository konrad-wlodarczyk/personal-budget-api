package com.softnet.budgetapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request){
        return createProblemDetail(
                HttpStatus.NOT_FOUND,
                "Resource Not Found",
                ex.getMessage(),
                ex.getErrorCode(),
                request.getRequestURI()
        );
    }

    //409
    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusinessRuleConflictException(BusinessException ex, HttpServletRequest request){
        return createProblemDetail(
                HttpStatus.CONFLICT,
                "Business Rule Conflict",
                ex.getMessage(),
                ex.getErrorCode(),
                request.getRequestURI()
        );
    }

    //400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request){
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return createProblemDetail(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                errorMessage,
                ErrorCode.VALIDATION_FAILED,
                request.getRequestURI()
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleReadable(HttpMessageNotReadableException ex,
                                        HttpServletRequest request) {

        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMostSpecificCause().getMessage()
        );
    }

    //500
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllUncaughtExceptions(Exception ex, HttpServletRequest request){
        return createProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occured. Please try again",
                ErrorCode.INTERNAL_SERVER_ERROR,
                request.getRequestURI()
        );
    }

    private ProblemDetail createProblemDetail(HttpStatus status, String title, String detail, ErrorCode errorCode, String uri){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setInstance(URI.create(uri));
        problemDetail.setProperty("code", errorCode.name());
        problemDetail.setProperty("timestamp", OffsetDateTime.now());
        return problemDetail;
    }
}
