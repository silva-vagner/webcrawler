package com.webcrawler.backend.model;

import java.util.ArrayList;
import java.util.List;

public class SearchFileModel {
    private String id;
    private String status;
    private List<String> urls;

    public SearchFileModel(String id, String status) {
        this.id = id;
        this.status = status;
        this.urls = new ArrayList<>();
    }

    public SearchFileModel(String id, String done, List<String> strings) {
        this.id = id;
        this.status = done;
        this.urls = strings;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }
}
