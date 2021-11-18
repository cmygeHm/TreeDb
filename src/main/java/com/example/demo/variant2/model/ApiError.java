package com.example.demo.variant2.model;

import javax.annotation.Nonnull;

public class ApiError {
    @Nonnull
    private String error;

    public ApiError(@Nonnull String error) {
        this.error = error;
    }

    @Nonnull
    public String getError() {
        return error;
    }

    public void setError(@Nonnull String error) {
        this.error = error;
    }
}
