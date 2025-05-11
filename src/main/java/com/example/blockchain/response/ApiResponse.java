package com.example.blockchain.response;

import java.time.Instant;

public class ApiResponse {
    private final String message;
    private final int status;
    private final long timestamp;

    public ApiResponse(String message, int status) {
        this.timestamp = Instant.now().toEpochMilli();
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
