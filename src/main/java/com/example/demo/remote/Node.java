package com.example.demo.remote;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class Node {
    @Nonnull
    private final Long id;
    @Nonnull
    private final Long parentId;
    @Nonnull
    private final Set<Node> childNodes;
    @Nonnull
    private Boolean isDeleted;

    private Node(@Nonnull Long id,
                 @Nonnull Long parentId,
                 @Nonnull Set<Node> childNodes,
                 @Nonnull Boolean isDeleted
    ) {
        this.id = requireNonNull(id, "id");
        this.parentId = requireNonNull(parentId, "parentId");
        this.childNodes = requireNonNull(childNodes, "childNodes");
        this.isDeleted = requireNonNull(isDeleted, "isDeleted");
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
    public Set<Node> getChildNodes() {
        return childNodes;
    }

    @Nonnull
    public Boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean isDeleted, Consumer<Long> consumer) {
        this.isDeleted = isDeleted;
        consumer.accept(this.id);

        childNodes.forEach(n -> n.setDeleted(isDeleted, consumer));
    }

    @Nonnull
    public void addChildNode(Node child) {
        childNodes.add(child);
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
        private final Set<Node> childNodes;
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

        public Builder withIsDeleted(@Nonnull Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        @Nonnull
        public Node build() {
            return new Node(id,
                    parentId,
                    childNodes,
                    isDeleted
                    );
        }
    }
}
