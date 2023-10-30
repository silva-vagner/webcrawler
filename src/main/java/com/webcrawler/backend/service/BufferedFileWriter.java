package com.webcrawler.backend.service;

import com.webcrawler.backend.model.SearchFileModel;
import com.webcrawler.backend.model.UpdateFileArgumentsModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class BufferedFileWriter {
    private Deque<UpdateFileArgumentsModel> buffer = new LinkedBlockingDeque<>();
    private String jsonFileName;
    private ExecutorService writerExecutor = Executors.newSingleThreadExecutor();
    private static final Logger logger = LoggerFactory.getLogger(BufferedFileWriter.class);


    public BufferedFileWriter(String jsonFileName) {
        this.jsonFileName = jsonFileName;
        startWriterThread();
    }

    private void startWriterThread() {
        logger.info("Thread de gravação iniciada: {}", Thread.currentThread().getName());
        writerExecutor.execute(() -> {
            while (true) {
                try {
                    UpdateFileArgumentsModel updateArguments = buffer.poll();
                    if (updateArguments != null) {
                        updateFile(updateArguments);
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    public void write(UpdateFileArgumentsModel updateArguments) {
        buffer.add(updateArguments);
        logger.info("Objeto adicionado ao buffer: {}", updateArguments);
    }

    public void writeWithHigherPriority(UpdateFileArgumentsModel updateArguments) {
        buffer.addFirst(updateArguments);
        logger.info("Objeto adicionado ao inicio da fila do buffer: {}", updateArguments);
    }

    private void createFile() {
        try (FileWriter writer = new FileWriter(jsonFileName)) {
            writer.write("");
            logger.info("Arquivo criado");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateFile(UpdateFileArgumentsModel updateArguments) {
        synchronized (jsonFileName) {
            if(updateArguments == null){
                createFile();
                return;
            }

            String searchId = updateArguments.getSearchId();
            String currentVisitedURL = updateArguments.getCurrentVisitedURL();
            String status = updateArguments.getStatus();
            Gson gson = new Gson();
            List<SearchFileModel> listFromFile = openFile();

            if (listFromFile == null || listFromFile.isEmpty()) {
                listFromFile = new ArrayList<>();
            }

            SearchFileModel currentEntity = listFromFile.stream()
                    .filter(item -> item.getId().equals(searchId))
                    .findFirst()
                    .orElse(null);

            if (currentEntity == null) {
                currentEntity = new SearchFileModel(searchId, "ACTIVE");
                listFromFile.add(currentEntity);
            }

            if (status != null) {
                currentEntity.setStatus(status);
            } else {
                if (currentVisitedURL == null) {
                    currentEntity.setUrls(new ArrayList<>());
                } else {
                    currentEntity.getUrls().add(currentVisitedURL);
                }
            }

            String newStringFile = gson.toJson(listFromFile);

            try (FileWriter writer = new FileWriter(jsonFileName)) {
                writer.write(newStringFile);
                logger.info("Arquivo atualizado com o objeto: {}", currentEntity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<SearchFileModel> openFile() {
        logger.info("Abrindo arquivo");
        synchronized (jsonFileName) {
            Gson gson = new Gson();
            try (FileReader reader = new FileReader(jsonFileName)) {
                Type listType = new TypeToken<List<SearchFileModel>>() {
                }.getType();
                return gson.fromJson(reader, listType);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return List.of();
            } catch (IOException e) {
                e.printStackTrace();
                createFile();
                return null;
            }
        }
    }
}
