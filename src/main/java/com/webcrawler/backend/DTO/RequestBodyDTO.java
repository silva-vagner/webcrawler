package com.webcrawler.backend.DTO;

public class RequestBodyDTO {
    private String keyword;

    public RequestBodyDTO(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}
