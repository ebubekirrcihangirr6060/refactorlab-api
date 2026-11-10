package com.refactorlab.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import tools.jackson.core.JacksonException;

import java.util.stream.Collectors;

// Merkezi (global) hata yönetimi: tüm controller'lardaki hatalar burada JSON'a çevrilir
@RestControllerAdvice
public class RefactorLabRestExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RefactorLabRestExceptionHandler.class);

    // 404 - kod parçası bulunamadı
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(CodeSnippetNotFoundException exc) {
        return buildResponse(HttpStatus.NOT_FOUND, exc.getMessage());
    }

    // 404 - kural bulunamadı
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(RefactoringRuleNotFoundException exc) {
        return buildResponse(HttpStatus.NOT_FOUND, exc.getMessage());
    }

    // 404 - olmayan URL
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(NoResourceFoundException exc) {
        return buildResponse(HttpStatus.NOT_FOUND, "Resource not found - /" + exc.getResourcePath());
    }

    // 400 - uygulamaya özgü geçersiz istek
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(InvalidRequestException exc) {
        return buildResponse(HttpStatus.BAD_REQUEST, exc.getMessage());
    }

    // 400 - Bean Validation hataları (@Valid)
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(MethodArgumentNotValidException exc) {
        String theMessage = exc.getBindingResult().getFieldErrors().stream()
                .map(theError -> theError.getField() + ": " + theError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed - " + theMessage);
    }

    // 400 - bozuk / okunamayan JSON gövdesi
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(HttpMessageNotReadableException exc) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed JSON request body");
    }

    // 400 - URL'deki id sayı değil (ör. /api/snippets/abc)
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(MethodArgumentTypeMismatchException exc) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + exc.getName() + "': " + exc.getValue());
    }

    // 405 - desteklenmeyen HTTP metodu
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(HttpRequestMethodNotSupportedException exc) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, exc.getMessage());
    }

    // 400 - PATCH gövdesindeki bir alan entity'ye dönüştürülemedi (ör. "title": {})
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(JacksonException exc) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid field value in request body");
    }

    // 400 - veritabanı kısıtı ihlali (ör. aynı smellName ile eşzamanlı ekleme)
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(DataIntegrityViolationException exc) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Request violates a database constraint");
    }

    // Diğer tüm hatalar (catch-all):
    //  - Spring'in istemci hataları (415, 400 eksik parametre vb.) kendi durum koduyla döner
    //  - gerçek sunucu hataları (DB kapalı, beklenmeyen hata) 500 döner, iç detay dışarı verilmez
    @ExceptionHandler
    public ResponseEntity<RefactorLabErrorResponse> handleException(Exception exc) {
        if (exc instanceof ErrorResponse theSpringError) {
            HttpStatus theStatus = HttpStatus.valueOf(theSpringError.getStatusCode().value());
            return buildResponse(theStatus, exc.getMessage());
        }
        logger.error("Unexpected server error", exc);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error - please try again later");
    }

    private ResponseEntity<RefactorLabErrorResponse> buildResponse(HttpStatus theStatus, String theMessage) {
        RefactorLabErrorResponse theError = new RefactorLabErrorResponse(
                theStatus.value(), theMessage, System.currentTimeMillis());
        return new ResponseEntity<>(theError, theStatus);
    }
}
