package com.wonderx.rwe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rwe.ocr")
@Getter
@Setter
public class OcrProperties {
    private String serviceUrl;
    private boolean mockEnabled = true;
}
