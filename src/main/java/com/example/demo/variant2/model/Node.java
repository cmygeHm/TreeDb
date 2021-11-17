package com.example.demo.variant2.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class Node {
    @Nonnull
    private final Long id;
    @Nullable
    private Long parentId;
    @Nullable
    private String value;
    @Nonnull
    private final List<Node> nodes;
    @Nonnull
    private boolean isDeleted = false;

    public Node(
            @Nonnull Long id,
            @Nullable String value,
            @Nonnull List<Node> nodes,
            @Nullable Long parentId
    ) {
        this.id = requireNonNull(id, "id");
        this.value = value;
        this.nodes = nodes;
        this.parentId = parentId;
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
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

    @Nullable
    public String getValue() {
        return value;
    }
    @Nonnull
    public List<Node> getNodes() {
        return nodes;
    }

    public void addNode(Node node) {
        node.setParentId(id);
        this.nodes.add(node);
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public void markAsDeleted() {
        isDeleted = true;
        if (nodes == null) {
            return;
        }
        for(Node node : nodes) {
            node.markAsDeleted();
        }
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

    public void setValue(String value) {
        this.value = value;
    }

    public static final class Builder {
        private Long id;
        private Long parentId;
        private String value;
        private final List<Node> nodes;

        private Builder() {
            id = IdGenerator.getId();
            nodes = new ArrayList<>();
        }

        public Builder withId(@Nonnull Long id) {
            this.id = id;
            return this;
        }

        public Builder withValue(@Nullable String value) {
            this.value = value;
            return this;
        }

        public Builder withParentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder addChild(@Nonnull Node node) {
            node.setParentId(id);
            this.nodes.add(node);
            return this;
        }

        @Nonnull
        public Node build() {
            if (value == null) {
                value = "node" + id;
            }
            return new Node(id, value, nodes, parentId);
        }
    }
}
