package com.wonderx.rwe.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rwe.otp")
@Getter
@Setter
public class OtpProperties {

    private int expirationSeconds;
    private int length;
}
