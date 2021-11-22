package com.example.demo.remote;

import com.example.demo.service.IdGenerator;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class Repository {

    @Nonnull
    private Map<Long, Record> records = new HashMap<>();

    @Nonnull
    private Node localCacheTree;

    public Repository() {
        reset();
    }

    public Map<Long, Record> reset() {
        IdGenerator.reset();
        records.clear();
        Record root = Record.builder()
            .withId(IdGenerator.getId())
            .withParentId(0L)
            .build();

        Record n1 = createRecord(root);
        Record n2 = createRecord(root);
        Record n3 = createRecord(root);
        Record n11 = createRecord(n1);
        Record n12 = createRecord(n1);
        Record n13 = createRecord(n1);
        Record n21 = createRecord(n2);
        Record n22 = createRecord(n2);
        Record n23 = createRecord(n2);
        Record n111 = createRecord(n11);
        Record n112 = createRecord(n11);
        Record n113 = createRecord(n11);

        Record n1111 = createRecord(n111);
        Record n1112 = createRecord(n111);

        records.put(root.getId(), root);
        records.put(n1.getId(), n1);
        records.put(n2.getId(), n2);
        records.put(n3.getId(), n3);
        records.put(n11.getId(), n11);
        records.put(n12.getId(), n12);
        records.put(n13.getId(), n13);
        records.put(n21.getId(), n21);
        records.put(n22.getId(), n22);
        records.put(n23.getId(), n23);
        records.put(n111.getId(), n111);
        records.put(n112.getId(), n112);
        records.put(n113.getId(), n113);
        records.put(n1111.getId(), n1111);
        records.put(n1112.getId(), n1112);

        initTree();

        return records;
    }

    private Record createRecord(Record parentRecord) {
        return Record.builder()
                .withId(IdGenerator.getId())
                .withParentId(parentRecord.getId())
                .build();
    }

    public Record getById(Long id, Set<Long> localKeys) {
        Record record = records.get(id);

        return Record.builder()
                .withId(record.getId())
                .withValue(record.getValue())
                .withIsDeleted(record.isDeleted())
                .withParentId(record.getParentId())
                .build();
    }

    public Map<Long, Record> getAll() {
        return records;
    }

    public Set<Long> apply(@Nonnull Map<Long, Record> updatedRecords,
                      @Nonnull Set<Long> deletedParents) {
        records.putAll(updatedRecords);

        Set<Long> deletedIds = new HashSet<>(records.size());
        return deleteRecords(localCacheTree, deletedParents, deletedIds);
    }

    private Set<Long> deleteRecords(
            Node topNode,
            Set<Long> deletedParents,
            Set<Long> deletedIds
    ) {
        if (deletedParents.contains(topNode.getId())) {
            if (!topNode.isDeleted()) {
                topNode.setDeleted(true, deletedIds::add);
            }
        } else if (!topNode.getChildNodes().isEmpty()) {
            for (Node child: topNode.getChildNodes()) {
                deleteRecords(child, deletedParents, deletedIds);
            }
        }

        records.entrySet()
                .stream()
                .filter(record -> deletedIds.contains(record.getKey()))
                .forEach(record -> record.getValue().setDeleted(true));

        return deletedIds;
    }

    private void initTree() {
        Map<Long, Node> nodes = new HashMap<>(records.size());
        records.forEach((key, value) -> {
            Node node = Node.builder()
                    .withId(value.getId())
                    .withParentId(value.getParentId())
                    .withIsDeleted(value.isDeleted())
                    .build();
            nodes.put(node.getId(), node);
        });

        nodes.forEach((key, value) -> {
            Node parent = nodes.get(value.getParentId());
            if (parent != null) {
                parent.addChildNode(value);
            }
        });

        localCacheTree = nodes.entrySet()
                .stream()
                .filter(entry -> entry.getValue().getParentId() == 0L)
                .findFirst()
                .get()
                .getValue();
    }
}
