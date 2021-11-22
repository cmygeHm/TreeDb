package com.example.demo.service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class RemoteDb {

    @Nonnull
    private Map<Long, Record> records = new HashMap<>();

    public RemoteDb() {
        reset();
    }

    public Map<Long, Record> reset() {
        IdGenerator.reset();
        records.clear();
        Record root = Record.builder()
            .withId(IdGenerator.getId())
            .withParentId(0L)
            .withParentIds(new HashSet<>())
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

        return records;
    }

    private Record createRecord(Record parentRecord) {
        return Record.builder()
                .withId(IdGenerator.getId())
                .withParentId(parentRecord.getId())
                .withParentIds(parentRecord.getParentIds())
                .build();
    }

    public Record getById(Long id, Set<Long> localKeys) {
        Record record = records.get(id);
        Set<Long> filteredParents = record.getParentIds()
                .stream()
                .filter(localKeys::contains)
                .collect(Collectors.toSet());

        return Record.builder()
                .withId(record.getId())
                .withValue(record.getValue())
                .withIsDeleted(record.isDeleted())
                .withParentId(record.getParentId())
                .withParentIds(filteredParents)
                .build();
    }

    public Map<Long, Record> getAll() {
        return records;
    }

    public void apply(@Nonnull Map<Long, Record> updatedRecords,
                      @Nonnull Set<Long> deletedParents) {
        records.putAll(updatedRecords);
        records.entrySet()
                .stream()
                .filter(record -> !Collections.disjoint(record.getValue().getParentIds(), deletedParents))
                .forEach(record -> record.getValue().setDeleted(true));
    }
}
