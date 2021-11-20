package com.example.demo.model;

import javax.annotation.Nullable;

public class NodeValue {
    @Nullable
    private String value;

    @Nullable
    public String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }
}
