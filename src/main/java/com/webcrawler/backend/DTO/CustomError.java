package com.webcrawler.backend.DTO;

public class CustomError {
    private int status;
    private String message;

    public CustomError(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

