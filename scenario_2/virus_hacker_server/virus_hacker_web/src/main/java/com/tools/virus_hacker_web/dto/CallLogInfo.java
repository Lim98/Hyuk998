package com.tools.virus_hacker_web.dto;

import lombok.Data;

@Data
public class CallLogInfo {
    private String number;
    private String name;
    private Long date;
    private Long duration;
    private String type;
}
