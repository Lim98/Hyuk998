package com.tools.virus_hacker_web.service;

import com.tools.virus_hacker_web.dto.*;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class CsvService {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 모든 데이터를 하나의 CSV 파일에 저장
     */
    public void createComprehensiveCsv(CollectedData data, Path filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toFile(), false)) {
            // UTF-8 BOM 추가 (엑셀에서 한글 깨짐 방지)
            writer.write('\ufeff');

            // 연락처 섹션
            if (data.getContacts() != null && !data.getContacts().isEmpty()) {
                writeSectionHeader(writer, "=== 연락처 ===");
                writeContacts(writer, data.getContacts());
                writer.write("\n");
            }

            // SMS 섹션
            if (data.getSms() != null && !data.getSms().isEmpty()) {
                writeSectionHeader(writer, "=== SMS ===");
                writeSms(writer, data.getSms());
                writer.write("\n");
            }

            // 통화기록 섹션
            if (data.getCallLogs() != null && !data.getCallLogs().isEmpty()) {
                writeSectionHeader(writer, "=== 통화기록 ===");
                writeCallLogs(writer, data.getCallLogs());
                writer.write("\n");
            }

            // 미디어파일 섹션
            if (data.getMediaFiles() != null && !data.getMediaFiles().isEmpty()) {
                writeSectionHeader(writer, "=== 미디어파일 ===");
                writeMediaFiles(writer, data.getMediaFiles());
                writer.write("\n");
            }

            // 문서 섹션
            if (data.getDocuments() != null && !data.getDocuments().isEmpty()) {
                writeSectionHeader(writer, "=== 문서 ===");
                writeDocuments(writer, data.getDocuments());
                writer.write("\n");
            }

            // 기기정보 섹션
            if (data.getDeviceInfo() != null) {
                writeSectionHeader(writer, "=== 기기정보 ===");
                writeDeviceInfo(writer, data.getDeviceInfo());
                writer.write("\n");
            }
        }
    }

    private void writeSectionHeader(FileWriter writer, String header) throws IOException {
        writer.write(header);
        writer.write("\n");
    }

    private void writeContacts(FileWriter writer, List<ContactInfo> contacts) throws IOException {
        // 헤더
        writer.write("이름,전화번호,이메일\n");
        
        // 데이터
        for (ContactInfo contact : contacts) {
            writer.write(escapeCsv(contact.getName()) + ",");
            writer.write(escapeCsv(contact.getPhone()) + ",");
            writer.write(escapeCsv(contact.getEmail()) + "\n");
        }
    }

    private void writeSms(FileWriter writer, List<SmsInfo> smsList) throws IOException {
        // 헤더
        writer.write("주소,내용,날짜,타입\n");
        
        // 데이터
        for (SmsInfo sms : smsList) {
            writer.write(escapeCsv(sms.getAddress()) + ",");
            writer.write(escapeCsv(sms.getBody()) + ",");
            writer.write(escapeCsv(formatDate(sms.getDate())) + ",");
            writer.write(escapeCsv(sms.getType()) + "\n");
        }
    }

    private void writeCallLogs(FileWriter writer, List<CallLogInfo> callLogs) throws IOException {
        // 헤더
        writer.write("번호,이름,날짜,통화시간(초),타입\n");
        
        // 데이터
        for (CallLogInfo callLog : callLogs) {
            writer.write(escapeCsv(callLog.getNumber()) + ",");
            writer.write(escapeCsv(callLog.getName()) + ",");
            writer.write(escapeCsv(formatDate(callLog.getDate())) + ",");
            writer.write(escapeCsv(callLog.getDuration() != null ? callLog.getDuration().toString() : "") + ",");
            writer.write(escapeCsv(callLog.getType()) + "\n");
        }
    }

    private void writeMediaFiles(FileWriter writer, List<MediaFileInfo> mediaFiles) throws IOException {
        // 헤더
        writer.write("경로,이름,크기(바이트),MIME 타입,추가일\n");
        
        // 데이터
        for (MediaFileInfo mediaFile : mediaFiles) {
            writer.write(escapeCsv(mediaFile.getPath()) + ",");
            writer.write(escapeCsv(mediaFile.getName()) + ",");
            writer.write(escapeCsv(mediaFile.getSize() != null ? mediaFile.getSize().toString() : "") + ",");
            writer.write(escapeCsv(mediaFile.getMimeType()) + ",");
            writer.write(escapeCsv(formatDate(mediaFile.getDateAdded())) + "\n");
        }
    }

    private void writeDocuments(FileWriter writer, List<DocumentInfo> documents) throws IOException {
        // 헤더
        writer.write("경로,이름,크기(바이트),MIME 타입\n");
        
        // 데이터
        for (DocumentInfo document : documents) {
            writer.write(escapeCsv(document.getPath()) + ",");
            writer.write(escapeCsv(document.getName()) + ",");
            writer.write(escapeCsv(document.getSize() != null ? document.getSize().toString() : "") + ",");
            writer.write(escapeCsv(document.getMimeType()) + "\n");
        }
    }

    private void writeDeviceInfo(FileWriter writer, DeviceInfo deviceInfo) throws IOException {
        // 헤더
        writer.write("항목,값\n");
        
        // 데이터
        writer.write("모델," + escapeCsv(deviceInfo.getModel()) + "\n");
        writer.write("제조사," + escapeCsv(deviceInfo.getManufacturer()) + "\n");
        writer.write("안드로이드 버전," + escapeCsv(deviceInfo.getAndroidVersion()) + "\n");
        writer.write("SDK 버전," + escapeCsv(deviceInfo.getSdkVersion() != null ? deviceInfo.getSdkVersion().toString() : "") + "\n");
    }

    /**
     * CSV 필드 이스케이프 처리 (쉼표, 따옴표, 줄바꿈 등)
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        
        // 쉼표, 따옴표, 줄바꿈이 포함된 경우 따옴표로 감싸고 내부 따옴표는 두 개로 변환
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }

    private String formatDate(Long timestamp) {
        if (timestamp == null) {
            return "";
        }
        return dateFormat.format(new Date(timestamp));
    }
}

