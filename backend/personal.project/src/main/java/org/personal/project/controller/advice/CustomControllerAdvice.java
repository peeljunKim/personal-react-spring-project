package org.personal.project.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class CustomControllerAdvice {

    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<?> notExist(NoSuchElementException e) {

        String msg = e.getMessage();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("msg", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleIllegalArgumentException(MethodArgumentNotValidException e) {

        String msg = e.getMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("msg", msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {

        String msg = e.getMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("msg", msg));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<?> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {

        String msg = e.getMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("msg", msg));
    }
}
