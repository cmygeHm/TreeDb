package com.example.demo.variant2.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class NodeDraft {
    @Nullable
    private Long id;
    @Nullable
    private Long parentId;
    @Nullable
    private String value;
    @Nullable
    private List<Node> nodes;
    @Nonnull
    private boolean isDeleted = false;

    public NodeDraft(
            @Nullable Long id,
            @Nullable String value,
            @Nullable List<Node> nodes,
            @Nullable Long parentId
    ) {
        this.id = id;
        this.value = value;
        this.nodes = nodes;
        this.parentId = parentId;
    }

    public NodeDraft(
            @Nullable Long id
    ) {
        this.id = id;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    @Nullable
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(@Nullable Long parentId) {
        this.parentId = parentId;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }

    @Nullable
    public List<Node> getNodes() {
        return nodes;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
