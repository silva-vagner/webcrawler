package com.webcrawler.backend.controller;

import com.webcrawler.backend.DTO.CreatedSearchResponseDTO;
import com.webcrawler.backend.DTO.CustomError;
import com.webcrawler.backend.DTO.RequestBodyDTO;
import com.webcrawler.backend.DTO.ResponseContentDTO;
import com.webcrawler.backend.service.BufferedFileWriter;
import com.webcrawler.backend.service.CrawlingService;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static spark.Spark.get;
import static spark.Spark.post;

public class CrawlingController {
    public static void init() {
        ExecutorService executor;
        executor = Executors.newCachedThreadPool();
        BufferedFileWriter bufferedFileWriter = new BufferedFileWriter("search-result.json");
        Gson gson = new Gson();

        get("/crawl/:id", (req, res) -> {
            CrawlingService crawlingService = new CrawlingService(req, bufferedFileWriter);
            String id = req.params("id");
            ResponseContentDTO result = crawlingService.searchCrawlById(id);
            if(result.getId() == null){
                res.status(404);
                res.type("application/json");
                CustomError customError = new CustomError(404, "crawl not found: " + id);

                res.body(gson.toJson(customError));
                return res.body();
            }
            res.status(200);
            res.type("application/json");
            res.body(gson.toJson(result));
            return res.body();
        });

        post("/crawl", (req, res) -> {
            RequestBodyDTO requestBody = gson.fromJson(req.body(), RequestBodyDTO.class);
            if(requestBody.getKeyword().length() < 4 || requestBody.getKeyword().length() > 32){
                CustomError customError = new CustomError(400, "field 'keyword' is required (from 4 up to 32 chars)");
                String responseBody = gson.toJson(customError);
                res.body(responseBody);
                res.status(400);
                res.type("application/json");
                return res.body();
            }
            CrawlingService crawlingService = new CrawlingService(req, bufferedFileWriter);
            crawlingService.createNewSearch();

            executor.submit(crawlingService);

            CreatedSearchResponseDTO response = new CreatedSearchResponseDTO(crawlingService.getRandomStringGenerated());
            String responseBody = gson.toJson(response);
            res.status(200);
            res.type("application/json");
            res.body(responseBody);

            return res.body();
        });
    }
}
