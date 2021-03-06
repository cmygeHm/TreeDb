package com.example.demo.controller;

import com.example.demo.model.ApiError;
import com.example.demo.model.NodeValue;
import com.example.demo.service.IdGenerator;
import com.example.demo.model.Node;
import com.example.demo.service.NodeFactory;
import com.example.demo.service.Record;
import com.example.demo.service.RemoteDb;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/tree")
public class TreeController {
    @Autowired
    private RemoteDb remoteDb;

    private Map<Long, Node> roots = new HashMap<>();
    private Map<Long, Node> nodesMap = new HashMap<>();

    @GetMapping("/load-origin-tree")
    public List<Node> loadOriginTree() {
        remoteDb.reset();
        roots = new HashMap<>();
        nodesMap = new HashMap<>();

        return Collections.singletonList(
                NodeFactory.buildTree(new HashMap<>(remoteDb.getAll()))
        );
    }

    @PostMapping("/apply")
    public List<Node> apply() {
        Map<Long, Record> updatedRecords = new HashMap<>(nodesMap.size());
        Set<Long> deletedParents = new HashSet<>(nodesMap.size());
        nodesMap.forEach((key, node) -> {
            Record record = Record.builder()
                    .withId(node.getId())
                    .withParentId(node.getParentId())
                    .withValue(node.getValue())
                    .withParentIds(node.getParentIds())
                    .withIsDeleted(node.isDeleted())
                    .build();
            updatedRecords.put(record.getId(), record);
            if (record.isDeleted()) {
                deletedParents.add(record.getId());
            }
        });

        remoteDb.apply(updatedRecords, deletedParents);

        return Collections.singletonList(
                NodeFactory.buildTree(new HashMap<>(remoteDb.getAll()))
        );
    }

    @PostMapping("/node/{nodeId}/copy")
    public ResponseEntity<List<Node>> copy(@PathVariable(value="nodeId") Long id) {
        if (nodesMap.get(id) != null) {
            return createErrorResponse("?????????????? ?????? ???????????????????????? ?? ?????????????????? ????????");
        }
        var record = remoteDb.getById(id, nodesMap.keySet());
        if (record.isDeleted()) {
            return createErrorResponse("???????????? ???????????????????? ?????????????????? ??????????????");
        }
        Node node = Node.builder()
                .withId(record.getId())
                .withParentId(record.getParentId())
                .withValue(record.getValue())
                .withIsDeleted(record.isDeleted())
                .withParentIds(record.getParentIds())
                .build();

        nodesMap.put(node.getId(), node);
        Node parent = nodesMap.get(record.getParentId());
        if (parent == null) {
            roots.put(node.getId(), node);
        } else {
            parent.addChildNode(node);
            node.setDeleted(parent.isDeleted());
        }

        for (Iterator<Map.Entry<Long, Node>> it = roots.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Long, Node> entry = it.next();
            Node aloneNode = entry.getValue();
            if (node.getId().equals(aloneNode.getParentId())) {
                node.addChildNode(aloneNode);
                it.remove();
            }
            if (
                    node.getParentIds().contains(aloneNode.getId()) &&
                    aloneNode.isDeleted()
            ) {
                node.setDeleted(true);
            }
        }

        return new ResponseEntity(new ArrayList(roots.values()), HttpStatus.OK);
    }

    @PostMapping("/node/{parentNodeId}/add-child")
    public ResponseEntity<Optional<List<Node>>> addChild(
            @PathVariable(value="parentNodeId") Long parentId,
            @RequestBody NodeValue nodeValue
    ) {
        Node parentNode = nodesMap.get(parentId);
        if (parentNode == null) {
            return createErrorResponse("???????????????????????? ?????????????? ???? ????????????");
        }
        if (parentNode.isDeleted()) {
            return createErrorResponse("???????????? ?????????????????? ?????????????? ?? ???????????????????? ????????????????");
        }
        if (nodeValue.getValue() == null) {
            return createErrorResponse("???????????????? ?????? ???????????????? ???? ???????? ????????????????");
        }

        var set = new HashSet<>(parentNode.getParentIds());
        Long id = IdGenerator.getId();
        set.add(id);

        Node node = Node.builder()
                .withId(id)
                .withParentId(parentId)
                .withValue(nodeValue.getValue())
                .withParentIds(set)
                .build();
        parentNode.addChildNode(node);
        nodesMap.put(node.getId(), node);

        return new ResponseEntity(new ArrayList(roots.values()), HttpStatus.OK);
    }

    @PatchMapping("/node/{nodeId}")
    public ResponseEntity<List<Node>> editNode(
            @PathVariable(value="nodeId") Long id,
            @RequestBody NodeValue nodeValue
    ) {
        Node node = nodesMap.get(id);
        if (node == null) {
            return createErrorResponse("?????????????? ???? ???????????? ?? ?????????????????? ????????");
        }
        if (node.isDeleted()) {
            return createErrorResponse("???????????? ?????????????????????????? ?????????????????? ??????????????");
        }

        node.setValue(nodeValue.getValue());

        return new ResponseEntity(new ArrayList(roots.values()), HttpStatus.OK);
    }

    @DeleteMapping("/node/{nodeId}")
    public ResponseEntity<List<Node>> deleteNode(
            @PathVariable(value="nodeId") Long id
    ) {
        Node nodeToDelete = nodesMap.get(id);
        if (nodeToDelete == null) {
            return createErrorResponse("?????????????? ???? ???????????? ?? ?????????????????? ????????");
        }
        nodeToDelete.setDeleted(true);

        roots.forEach((key, node) -> {
            if (node.getParentIds().contains(nodeToDelete.getId())) {
                node.setDeleted(true);
            }
        });

        return new ResponseEntity(new ArrayList(roots.values()), HttpStatus.OK);
    }

    private static ResponseEntity createErrorResponse(String errorMessage) {
        return new ResponseEntity(
                new ApiError(errorMessage),
                HttpStatus.BAD_REQUEST
        );
    }
}
