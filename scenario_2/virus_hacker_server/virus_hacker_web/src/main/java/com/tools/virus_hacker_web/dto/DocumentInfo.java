package com.tools.virus_hacker_web.dto;

import lombok.Data;

@Data
public class DocumentInfo {
    private String path;
    private String name;
    private Long size;
    private String mimeType;
}
