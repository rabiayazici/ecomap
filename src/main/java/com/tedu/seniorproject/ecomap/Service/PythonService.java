package com.tedu.seniorproject.ecomap.Service;

import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PythonService {
    private Process pythonProcess;
    private final String pythonScriptPath = "python/satellite_route.py";

    @PostConstruct
    public void startPythonServer() {
        try {
            // Python script'in bulunduğu dizini belirle
            Path scriptPath = Paths.get(System.getProperty("user.dir"), pythonScriptPath);
            File scriptFile = scriptPath.toFile();
            
            // Python ortamını başlat
            ProcessBuilder processBuilder = new ProcessBuilder("python", scriptFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true);
            
            pythonProcess = processBuilder.start();
            
            // Python çıktısını logla
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(pythonProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.info("Python: " + line);
                    }
                } catch (Exception e) {
                    log.error("Error reading Python output: " + e.getMessage());
                }
            }).start();
            
            log.info("Python server started successfully");
        } catch (Exception e) {
            log.error("Failed to start Python server: " + e.getMessage());
        }
    }

    public void stopPythonServer() {
        if (pythonProcess != null) {
            pythonProcess.destroy();
            log.info("Python server stopped");
        }
    }
} 