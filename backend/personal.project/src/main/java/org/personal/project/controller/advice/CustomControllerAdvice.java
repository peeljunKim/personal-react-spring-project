package org.personal.project.controller.advice;

import org.personal.project.util.CustomJWTException;
import org.personal.project.service.payment.PaymentException;
import org.personal.project.service.coupon.CouponException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CustomControllerAdvice.class);

    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<?> notExist(NoSuchElementException e) {
        String msg = e.getMessage();
        log.error("예외: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("msg", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleIllegalArgumentException(MethodArgumentNotValidException e) {
        String msg = e.getMessage();
        log.error("예외: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("msg", msg));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<?> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String msg = e.getMessage();
        log.error("예외: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("msg", msg));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<?> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String msg = e.getMessage();
        log.error("예외: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("msg", msg));
    }

    @ExceptionHandler(CustomJWTException.class)
    protected ResponseEntity<?> handleJWTException(CustomJWTException e) {
        String msg = e.getMessage();
        log.error("예외: {}", e.getMessage(), e);

        return ResponseEntity.ok().body(Map.of("error", msg));
    }

    @ExceptionHandler(PaymentException.class)
    protected ResponseEntity<?> handlePaymentException(PaymentException e) {
        String msg = e.getMessage();
        log.error("결제 예외: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("msg", msg));
    }

    @ExceptionHandler(CouponException.class)
    protected ResponseEntity<?> handleCouponException(CouponException e) {
        String msg = e.getMessage();
        log.error("쿠폰 예외: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("msg", msg));
    }
}
