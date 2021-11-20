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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tree")
public class TreeController {
    @Autowired
    private RemoteDb remoteDb;

    private Map<Long, Node> roots = new HashMap<>();
    private Map<Long, Node> nodesMap = new HashMap<>();
    private Map<Long, List<Node>> newNodesMap = new HashMap<>();

    @GetMapping("/load-origin-tree")
    public List<Node> loadOriginTree() {
        remoteDb.reset();
        roots = new HashMap<>();
        nodesMap = new HashMap<>();
        newNodesMap = new HashMap<>();

        return Collections.singletonList(
                NodeFactory.buildTree(new HashMap<>(remoteDb.getAll()))
        );
    }

    @PostMapping("/apply")
    public List<Node> apply() {
        Node remoteTree = NodeFactory.buildTree(new HashMap<>(remoteDb.getAll()));
        Map<Long, Record> updatedRecords = new HashMap<>();

        recursiveTraversal(remoteTree, updatedRecords);

        remoteDb.apply(updatedRecords);

        return Collections.singletonList(
                NodeFactory.buildTree(remoteDb.getAll())
        );
    }

    private void recursiveTraversal(Node topNode, Map<Long, Record> updatedRecords) {
        Node localNode = nodesMap.get(topNode.getId());
        if (localNode != null) {
            if (localNode.isDeleted()) {
                topNode.setDeleted(localNode.isDeleted());
            }
            if (!localNode.getValue().equals(topNode.getValue())) {
                topNode.setValue(localNode.getValue());
            }
        }

        Record record = Record.builder()
                .withId(topNode.getId())
                .withParentId(topNode.getParentId())
                .withValue(topNode.getValue())
                .withParentIds(topNode.getParentIds())
                .witIsDeleted(topNode.isDeleted())
                .build();
        updatedRecords.put(topNode.getId(), record);

        List<Node> localChildNodes = newNodesMap.get(topNode.getId());
        if (localChildNodes != null) {
            Iterator<Node> i = localChildNodes.iterator();
            while (i.hasNext()) {
                Node newChild = i.next();
                Node t = nodesMap.get(newChild.getId());
                topNode.addChildNode(newChild);
                i.remove();
            }
        }

        for (Node child : topNode.getChildNodes()) {
            recursiveTraversal(child, updatedRecords);
        }
    }

    @PostMapping("/node/{nodeId}/copy")
    public ResponseEntity<List<Node>> copy(@PathVariable(value="nodeId") Long id) {
        if (nodesMap.get(id) != null) {
            return new ResponseEntity(
                    new ApiError("element already copied"),
                    HttpStatus.BAD_REQUEST
            );
        }
        var record = remoteDb.getById(id);
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
            return new ResponseEntity(
                    new ApiError("Parent node not found"),
                    HttpStatus.BAD_REQUEST
            );
        }
        if (nodeValue.getValue() == null) {
            return new ResponseEntity(
                    new ApiError("Node value required"),
                    HttpStatus.BAD_REQUEST
            );
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
        newNodesMap.putIfAbsent(node.getParentId(), new ArrayList<>());
        newNodesMap.get(node.getParentId()).add(node);

        return new ResponseEntity(new ArrayList(roots.values()), HttpStatus.OK);
    }

    @PatchMapping("/node/{nodeId}")
    public ResponseEntity<List<Node>> editNode(
            @PathVariable(value="nodeId") Long id,
            @RequestBody NodeValue nodeValue
    ) {
        Node node = nodesMap.get(id);
        if (node == null) {
            return new ResponseEntity(
                    new ApiError("Element not found to edit"),
                    HttpStatus.BAD_REQUEST
            );
        }
        if (node.isDeleted()) {
            return new ResponseEntity(
                    new ApiError("You can't edit deleted node"),
                    HttpStatus.BAD_REQUEST
            );
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
            return new ResponseEntity(
                    new ApiError("Element not found to delete"),
                    HttpStatus.BAD_REQUEST
            );
        }
        nodeToDelete.setDeleted(true);

        roots.forEach((key, node) -> {
            if (node.getParentIds().contains(nodeToDelete.getId())) {
                node.setDeleted(true);
            }
        });

        return new ResponseEntity(new ArrayList(roots.values()), HttpStatus.OK);
    }
}
