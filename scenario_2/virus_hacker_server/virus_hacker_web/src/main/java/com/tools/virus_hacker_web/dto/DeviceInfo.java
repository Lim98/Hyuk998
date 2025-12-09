package com.tools.virus_hacker_web.dto;

import lombok.Data;

@Data
public class DeviceInfo {
    private String model;
    private String manufacturer;
    private String androidVersion;
    private Integer sdkVersion;
}