package com.example.demo.config;

import com.example.demo.service.RemoteDb;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Bean
    public RemoteDb remoteDb() {
        return new RemoteDb();
    }
}
