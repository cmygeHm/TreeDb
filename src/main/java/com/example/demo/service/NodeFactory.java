package com.example.demo.service;

import com.example.demo.model.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NodeFactory {

    public static Node buildTree(Map<Long, Record> records) {
       Map<Long, Node> nodes = new HashMap<>(records.size());
       records.forEach((key, value) -> {
           Node node = Node.builder()
                   .withId(value.getId())
                   .withParentId(value.getParentId())
                   .withValue(value.getValue())
                   .withIsDeleted(value.isDeleted())
                   .withParentIds(value.getParentIds())
                   .build();
           nodes.put(node.getId(), node);
       });

       nodes.forEach((key, value) -> {
           Node parent = nodes.get(value.getParentId());
           if (parent != null) {
               parent.addChildNode(value);
           }
       });

        Optional<Map.Entry<Long, Node>> root = nodes.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getParentId() == 0L)
                .findFirst();

        return root.get().getValue();
    }
}
