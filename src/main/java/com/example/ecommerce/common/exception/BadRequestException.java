package com.example.ecommerce.common.exception;


public class BadRequestException extends RuntimeException {
    public BadRequestException(String msg) { super(msg); }
}