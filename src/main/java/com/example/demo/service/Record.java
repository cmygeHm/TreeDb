package com.example.demo.service;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Record {
    @Nonnull
    private final Long id;
    @Nonnull
    private final Long parentId;
    @Nonnull
    private String value;
    @Nonnull
    private Boolean isDeleted;
    @Nonnull
    private final Set<Long> parentIds;

    private Record(@Nonnull Long id,
                   @Nonnull Long parentId,
                   @Nonnull String value,
                   @Nonnull Set<Long> parentIds,
                   @Nonnull Boolean isDeleted) {
        this.id = requireNonNull(id, "id");
        this.parentId = requireNonNull(parentId, "parentId");
        this.value = requireNonNull(value, "value");
        this.parentIds = requireNonNull(parentIds, "parentIds");
        parentIds.add(id);
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
    public String getValue() {
        return value;
    }

    public void setValue(@Nonnull String value) {
        this.value = requireNonNull(value, "value");;
    }

    @Nonnull
    public Boolean isDeleted() {
        return isDeleted;
    }

    public void markAsDeleted() {
        isDeleted = true;
    }

    @Nonnull
    public Set<Long> getParentIds() {
        return new HashSet<>(parentIds);
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", value='" + value + '\'' +
                ", isDeleted=" + isDeleted +
                ", parentIds=" + parentIds +
                '}';
    }

    public static final class Builder {
        private Long id;
        private Long parentId;
        private String value;
        private Boolean deleted = false;
        private Set<Long> parentIds;

        private Builder() {
        }

        public Builder withId(@Nonnull Long id) {
            this.id = id;
            return this;
        }

        public Builder withParentId(@Nonnull Long parentId) {
            this.parentId = parentId;
            return this;
        }

        public Builder withParentIds(@Nonnull Set<Long> parentIds) {
            this.parentIds = parentIds;
            return this;
        }

        public Builder withValue(@Nonnull String value) {
            this.value = value;
            return this;
        }

        public Builder witIsDeleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        @Nonnull
        public Record build() {
            if (value == null) {
                value = "node " + id;
            }
            return new Record(id, parentId, value, parentIds, deleted);
        }
    }
}
