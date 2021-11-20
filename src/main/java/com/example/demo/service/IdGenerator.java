package com.example.demo.service;

public class IdGenerator {
    private static Long id = 1L;
    public static Long getId() {
        return id++;
    }

    public static void reset() {
        id = 1L;
    }
}
