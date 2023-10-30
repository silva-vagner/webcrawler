package com.webcrawler.backend.model;

public class UpdateFileArgumentsModel {
    private String searchId;
    private String currentVisitedURL;
    private String status;

    public UpdateFileArgumentsModel() {
    }

    public UpdateFileArgumentsModel(String searchId, String currentVisitedURL) {
        this.searchId = searchId;
        this.currentVisitedURL = currentVisitedURL;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public String getCurrentVisitedURL() {
        return currentVisitedURL;
    }

    public void setCurrentVisitedURL(String currentVisitedURL) {
        this.currentVisitedURL = currentVisitedURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


