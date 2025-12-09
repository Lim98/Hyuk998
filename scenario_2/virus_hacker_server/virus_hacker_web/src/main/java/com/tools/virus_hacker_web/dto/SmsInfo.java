package com.tools.virus_hacker_web.dto;

import lombok.Data;

@Data
public class SmsInfo {
    private String address;
    private String body;
    private Long date;
    private String type;
}
