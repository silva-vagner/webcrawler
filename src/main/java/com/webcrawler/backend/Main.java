package com.webcrawler.backend;

import com.webcrawler.backend.controller.CrawlingController;

import static spark.Spark.port;

public class Main {
    public static void main(String[] args) {
        port(4567);

        CrawlingController.init();
    }
}
