package com.example.demo.variant2.model;

public class IdGenerator {
    private static Long id = 0L;
    public static Long getId() {
        return id++;
    }
}
