package com.example.demo.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class NodeView {
    @Nonnull
    private final Long id;
    @Nullable
    private Long parentId;
    @Nonnull
    private final String value;
    @Nonnull
    private final List<Long> nodes;

    public NodeView(
            @Nonnull Long id,
            @Nonnull String value,
            @Nonnull List<Long> nodes,
            @Nullable Long parentId
    ) {
        this.id = requireNonNull(id, "id");
        this.value = "node" + id; //requireNonNull(value, "value");
        this.nodes = requireNonNull(nodes, "nodes");
        this.parentId = parentId;
    }

    @Nonnull
    public Long getId() {
        return id;
    }

    @Nullable
    public Long getParentId() {
        return parentId;
    }

    @Nonnull
    public void setParentId(@Nonnull Long parentId) {
        this.parentId = parentId;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Nonnull
    public List<Long> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", parentId='" + parentId + '\'' +
                ", value='" + value + '\'' +
                ", nodes=" + nodes +
                '}';
    }
}
