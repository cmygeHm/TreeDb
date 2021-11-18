package com.example.demo.model;

public class IdGenerator {
    private static Long id = 0L;
    public static Long getId() {
        return id++;
    }

    public static void reset() {
        id = 0L;
    }
}
