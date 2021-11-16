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
    private final String value;
    @Nullable
    private final List<Node> nodes;

    public Node(
            @Nonnull Long id,
            @Nullable String value,
            @Nullable List<Node> nodes,
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
    @Nullable
    public List<Node> getNodes() {
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

    public void addNode(Node node) {
        node.setParentId(id);
        this.nodes.add(node);
    }

    public static final class Builder {
        private final Long id;
        private String value;
        private final List<Node> nodes;

        private Builder() {
            id = IdGenerator.getId();
            nodes = new ArrayList<>();
        }

        public Builder withValue(@Nonnull String value) {
            this.value = value;
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
            return new Node(id, value, nodes, null);
        }
    }
}
