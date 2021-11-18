package com.example.demo.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CopyResult {
    private Node parentNode;
    private Node childNode;

    public CopyResult(
            @Nonnull Node childNode
    ) {
        this.childNode = childNode;
    }

    public CopyResult(
            @Nonnull Node childNode,
            @Nullable Node parentNode
    ) {
        this.parentNode = parentNode;
        this.childNode = childNode;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    public Node getChildNode() {
        return childNode;
    }

    public void setChildNode(Node childNode) {
        this.childNode = childNode;
    }
}
