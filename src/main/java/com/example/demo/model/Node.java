package com.example.demo.model;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Node {
    @Nonnull
    private final Long id;
    @Nonnull
    private final Long parentId;
    @Nonnull
    private String value;
    @Nonnull
    private final Set<Node> childNodes;
    @Nonnull
    private final Set<Long> parentIds;
    @Nonnull
    private Boolean isDeleted;

    private Node(@Nonnull Long id,
                 @Nonnull Long parentId,
                 @Nonnull String value,
                 @Nonnull Set<Node> childNodes,
                 @Nonnull Boolean isDeleted,
                 @Nonnull Set<Long> parentIds
    ) {
        this.id = requireNonNull(id, "id");
        this.parentId = requireNonNull(parentId, "parentId");
        this.value = requireNonNull(value, "value");
        this.childNodes = requireNonNull(childNodes, "childNodes");
        this.isDeleted = requireNonNull(isDeleted, "isDeleted");
        this.parentIds = requireNonNull(parentIds, "parentIds");
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public Long getId() {
        return id;
    }

    @Nonnull
    public Long getParentId() {
        return parentId;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Nonnull
    public Set<Node> getChildNodes() {
        return childNodes;
    }

    @Nonnull
    public Boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;

        childNodes.forEach(n -> n.setDeleted(isDeleted));
    }

    @Nonnull
    public void addChildNode(Node child) {
        childNodes.add(child);
    }

    @Nonnull
    public Set<Long> getParentIds() {
        return parentIds;
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", value='" + value + '\'' +
                ", childNodes=" + childNodes +
                ", parentIds=" + parentIds +
                ", isDeleted=" + isDeleted +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        if (!id.equals(node.id)) return false;
        return parentId.equals(node.parentId);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + parentId.hashCode();
        return result;
    }

    public static final class Builder {
        private Long id;
        private Long parentId;
        private String value;
        private final Set<Node> childNodes;
        private Set<Long> parentIds;
        private Boolean isDeleted = false;

        private Builder() {
            childNodes = new HashSet<>();
        }

        public Builder withId(@Nonnull Long id) {
            this.id = id;
            return this;
        }

        public Builder withParentId(@Nonnull Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder withValue(@Nonnull String value) {
            this.value = value;
            return this;
        }

        public Builder withIsDeleted(@Nonnull Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public Builder withParentIds(@Nonnull Set<Long> parentIds) {
            this.parentIds = parentIds;
            return this;
        }

        @Nonnull
        public Node build() {
            return new Node(id,
                    parentId,
                    value,
                    childNodes,
                    isDeleted,
                    parentIds);
        }
    }
}
