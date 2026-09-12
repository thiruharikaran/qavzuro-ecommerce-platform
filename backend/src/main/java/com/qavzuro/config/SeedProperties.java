package com.qavzuro.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.seed")
@Data
public class SeedProperties {
    private boolean enabled;
    private Account masterAdmin = new Account();
    private Account admin = new Account();

    @Data
    public static class Account {
        private String email;
        private String password;
    }
}
