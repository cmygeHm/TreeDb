package com.example.demo.remote;

import javax.annotation.Nonnull;
import static java.util.Objects.requireNonNull;

public class Record {
    @Nonnull
    private final Long id;
    @Nonnull
    private final Long parentId;
    @Nonnull
    private final String value;
    @Nonnull
    private Boolean isDeleted;

    private Record(@Nonnull Long id,
                   @Nonnull Long parentId,
                   @Nonnull String value,
                   @Nonnull Boolean isDeleted) {
        this.id = requireNonNull(id, "id");
        this.parentId = requireNonNull(parentId, "parentId");
        this.value = requireNonNull(value, "value");
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

    @Nonnull
    public Boolean isDeleted() {
        return isDeleted;
    }

    @Nonnull
    public void setDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return "Record{" +
                "id=" + id +
                ", parentId=" + parentId +
                ", value='" + value + '\'' +
                ", isDeleted=" + isDeleted +
                '}';
    }

    public static final class Builder {
        private Long id;
        private Long parentId;
        private String value;
        private Boolean deleted = false;

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

        public Builder withValue(@Nonnull String value) {
            this.value = value;
            return this;
        }

        public Builder withIsDeleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        @Nonnull
        public Record build() {
            if (value == null) {
                value = "node " + id;
            }
            return new Record(id, parentId, value, deleted);
        }
    }
}
