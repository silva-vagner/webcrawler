package com.webcrawler.backend.service;

import com.webcrawler.backend.DTO.RequestBodyDTO;
import com.webcrawler.backend.DTO.ResponseContentDTO;
import com.webcrawler.backend.model.SearchFileModel;
import com.webcrawler.backend.model.UpdateFileArgumentsModel;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import spark.Request;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlingService implements Runnable {
    static final String baseUrl = "BASE_URL";
    static final String stringBase = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final Logger logger = LoggerFactory.getLogger(CrawlingService.class);
    static SecureRandom rnd = new SecureRandom();
    private String randomStringGenerated = this.generateRandomString();

    private Queue<String> urlQueue;
    private List<String> visitedURLs;
    private List<String> matchedURLs = new ArrayList<>();
    private String rootURL;
    private String searchString;
    private Request req;
    private BufferedFileWriter bufferedFileWriter;

    public String getRandomStringGenerated() {
        return randomStringGenerated;
    }

    public CrawlingService(Request req, BufferedFileWriter bufferedFileWriter) {
        urlQueue = new LinkedList<>();
        visitedURLs = new ArrayList<>();
        this.rootURL = Objects.equals(System.getenv(baseUrl), "BASE_URL") ? System.getenv(baseUrl) : "https://www.scrapethissite.com/";
        this.req = req;
        this.bufferedFileWriter = bufferedFileWriter;
    }

    @Override
    public void run() {
        try {
            executeCrawl(req, rootURL, randomStringGenerated);
        } catch (ParserConfigurationException | IOException e) {
            e.printStackTrace();
        }
    }

    public void executeCrawl(Request req, String rootURL, String searchIdGenerated) throws ParserConfigurationException, IOException {
        Gson gson = new Gson();
        RequestBodyDTO requestBody = gson.fromJson(req.body(), RequestBodyDTO.class);

        this.searchString = requestBody.getKeyword();

        urlQueue.add(rootURL);
        visitedURLs.add(rootURL);

        while (!urlQueue.isEmpty()) {
            String currentVisitedURL = urlQueue.remove();
            String rawHTML = extractRawHTML(currentVisitedURL);

            scanHTML(rawHTML, rootURL);

            Matcher matcher = findMatchingURLs(rawHTML, searchString, currentVisitedURL);

            while (matcher.find()) {
                if (!matchedURLs.contains(currentVisitedURL)) {
                    UpdateFileArgumentsModel updateArguments = new UpdateFileArgumentsModel(searchIdGenerated, currentVisitedURL);
                    bufferedFileWriter.write(updateArguments);
                    matchedURLs.add(currentVisitedURL);
                }
            }
        }
        completeFileProcessing(searchIdGenerated);

    }

    private void completeFileProcessing(String searchIdGenerated){
        UpdateFileArgumentsModel updateArguments = new UpdateFileArgumentsModel();
        updateArguments.setSearchId(searchIdGenerated);
        updateArguments.setStatus("DONE");
        bufferedFileWriter.write(updateArguments);
    }

    private String extractRawHTML(String currentVisitedURL) {
        String rawHTML = "";
        try {
            URL url = new URL(currentVisitedURL);
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String inputLine = in.readLine();
                while (inputLine != null) {
                    rawHTML += inputLine;
                    inputLine = in.readLine();
                }
                in.close();
            } catch (IOException e) {
                logger.error("Erro recebendo o buffer da URL", e);
            }
        } catch (Exception e) {
            logger.error("Erro extraindo a URL", e);
        }
        return rawHTML;
    }

    public Matcher findMatchingURLs(String rawHTML, String searchString, String currentVisitedURL) {
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(searchString) + "\\b", Pattern.CASE_INSENSITIVE);

        return pattern.matcher(rawHTML);
    }

    private void scanHTML(String rawHTML, String rootUrl) {
        try {
            Pattern pattern = Pattern.compile("<a\\s+[^>]*href\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>(.*?)</a>", Pattern.CASE_INSENSITIVE);

            Matcher matcher = pattern.matcher(rawHTML);

            while (matcher.find()) {

                String foundedPath = matcher.group(1);
                String linkText = matcher.group(2);
                System.out.println("URL: " + foundedPath);
                System.out.println("Texto do link: " + linkText);
                String actualURL = "";
                if (isRelativeUrl(foundedPath)) {
                    if (foundedPath.equalsIgnoreCase("/")) {
                        actualURL = rootUrl;
                    } else {
                        actualURL = rootUrl + foundedPath;
                    }
                } else if (isValidDomain(rootUrl, foundedPath)) {
                    actualURL = foundedPath;
                } else {
                    continue;
                }

                if (!visitedURLs.contains(actualURL) && isValidDomain(rootUrl, actualURL)) {
                    visitedURLs.add(actualURL);
                    System.out.println("Website found with URL " + actualURL);
                    urlQueue.add(actualURL);
                }
            }
        } catch (Exception e) {
            logger.error("Erro no scan dos dados recebidos da pagina", e);
        }
    }

    private boolean isValidDomain(String rootURL, String actualURL) {
        try {
            URI rootUri = new URI(rootURL);
            URI actualUri = new URI(actualURL);
            return Objects.equals(rootUri.getHost(), actualUri.getHost());
        } catch (URISyntaxException e) {
            logger.error("Erro validando dominio da URL", e);
            return false;
        }
    }

    private boolean isRelativeUrl(String url) {
        return url.startsWith("/") || url.startsWith("./") || !url.startsWith("http://") && !url.startsWith("https://");
    }

    public String generateRandomString() {
        int len = 8;
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(stringBase.charAt(rnd.nextInt(stringBase.length())));
        return sb.toString();
    }

    public ResponseContentDTO searchCrawlById(String id){
        try{
            List<SearchFileModel> contentList = bufferedFileWriter.openFile();
            return contentList.stream()
                    .filter(searchObject -> searchObject.getId().equals(id))
                    .map(ResponseContentDTO::new)
                    .findFirst()
                    .orElseThrow();
        } catch (NoSuchElementException e){
            logger.error("Nenhum arquivo encontrado", e);
            return new ResponseContentDTO();
        } catch (NullPointerException e){
            logger.error("Criando um novo arquivo a partir da busca", e);
            bufferedFileWriter.write(null);
            return new ResponseContentDTO();
        }
    }

    public void createNewSearch() {
        UpdateFileArgumentsModel newSearch = new UpdateFileArgumentsModel(randomStringGenerated, null);
        bufferedFileWriter.writeWithHigherPriority(newSearch);
    }
}


