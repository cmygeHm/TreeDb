package com.example.demo.controller;

import com.example.demo.model.ApiError;
import com.example.demo.model.IdGenerator;
import com.example.demo.model.Node;
import com.example.demo.model.NodeDraft;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/v2/tree")
public class TreeController {

    private Map<Long, Node> localCache;
    private Map<Long, Map<Long, Node>> localCacheForNewNodes;
    private Node sourceNode = buildSourceNode();

    private Node buildSourceNode() {

        Node n = Node.builder()
                .addChild(generateNode(generateNode(generateNode(null))))
                .addChild(generateNode(generateNode(generateNode(null))))
                .addChild(generateNode(generateNode(generateNode(generateNode(generateNode(null))))))
                .build();

        return Node.builder()
                .addChild(n)
                .addChild(generateNode(generateNode(generateNode(null))))
                .addChild(generateNode(generateNode(generateNode(null))))
                .build();
    }

    private Node generateNode(@Nullable Node node) {
        Node.Builder builder = Node.builder()
                .withId(IdGenerator.getId());
        if (node != null) {
            builder.addChild(node);
        }

        return builder.build();
    }

    @GetMapping("/load-origin-tree")
    public Node loadOriginTree() {
        localCache = new HashMap<>();
        localCacheForNewNodes = new HashMap<>();
        IdGenerator.reset();
        sourceNode = buildSourceNode();
        return sourceNode;
    }

    @PostMapping("/apply")
    public Optional<Node> apply() {
        Node result = sourceNode;
        recursiveApply(result);
        return Optional.of(result);
    }

    private void recursiveApply(Node dataBaseNode) {
        if (localCache.containsKey(dataBaseNode.getId())) {
            Node cachedNode = localCache.get(dataBaseNode.getId());
            dataBaseNode.setValue(cachedNode.getValue());
            if (cachedNode.isDeleted()) {
                dataBaseNode.setDeleted(cachedNode.isDeleted());
            }
        }
        if (localCacheForNewNodes.containsKey(dataBaseNode.getId())) {
            for (Map.Entry<Long, Node> e : localCacheForNewNodes.get(dataBaseNode.getId()).entrySet()) {
                dataBaseNode.addNode(e.getValue());
            }

            localCacheForNewNodes.remove(dataBaseNode.getId());
        }
        for (Node child : dataBaseNode.getNodes()) {
            if (dataBaseNode.isDeleted()) {
                child.setDeleted(dataBaseNode.isDeleted());
            }
            recursiveApply(child);
        }
    }

    @PostMapping("/node/copy")
    public List<Node> nodeCopyV2(@RequestBody Node nodeToCopy) {

        localCache.put(nodeToCopy.getId(), nodeToCopy);

        return Collections.singletonList(nodeToCopy);
    }

    @DeleteMapping("/node")
    public Optional<Node> delete(@RequestBody Node nodeToDelete) {
        localCache.get(nodeToDelete.getId()).setDeleted(true);

        return Optional.empty();
    }

    @PatchMapping("/node")
    public Optional<Node> patchNodeValue(@RequestBody Node nodeToPatch) {

        localCache.get(nodeToPatch.getId()).setValue(nodeToPatch.getValue());

        return Optional.empty();
    }

    @PostMapping("/node")
    public ResponseEntity<Optional<Node>> createNewNode(@RequestBody NodeDraft nodeToCreate) {
        if (nodeToCreate.getParentId() == null) {
            return new ResponseEntity(
                    Optional.of(new ApiError("parentId required for create new node")),
                    HttpStatus.BAD_REQUEST
            );
        }
        Node newNode = Node.builder()
                .withId(IdGenerator.getId())
                .withParentId(nodeToCreate.getParentId())
                .withValue(nodeToCreate.getValue())
                .build();
        localCacheForNewNodes.putIfAbsent(newNode.getParentId(), new HashMap<>());
        localCacheForNewNodes.get(newNode.getParentId()).put(newNode.getId(), newNode);

        return new ResponseEntity(Optional.of(newNode), HttpStatus.OK);
    }
}
