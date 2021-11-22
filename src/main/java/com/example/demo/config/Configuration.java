package com.example.demo.config;

import com.example.demo.remote.Repository;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Bean
    public Repository remoteDb() {
        return new Repository();
    }
}
