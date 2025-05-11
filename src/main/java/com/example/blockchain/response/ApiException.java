package com.example.blockchain.response;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final String message;
    private final int status;

    public ApiException(String message, int status) {
        super(message);
        this.message = message;
        this.status = status;
    }

    public HttpStatus getStatus() {
        return HttpStatus.resolve(this.status);
    }
}
