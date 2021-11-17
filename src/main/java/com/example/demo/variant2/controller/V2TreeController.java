package com.example.demo.variant2.controller;

import com.example.demo.variant2.model.ApiError;
import com.example.demo.variant2.model.CopyResult;
import com.example.demo.variant2.model.IdGenerator;
import com.example.demo.variant2.model.Node;
import com.example.demo.variant2.model.NodeDraft;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/v2/tree")
public class V2TreeController {

    private List<Node> roots;
    private Map<Long, Node> localCache;
    private Map<Long, Map<Long, Node>> localCacheForNewNodes;
    private Node sourceNode = buildSourceNode();

    private Node buildSourceNode() {
        Node n6 = Node.builder().build();
        Node n7 = Node.builder().build();
        Node n5 = Node.builder().addChild(n6).addChild(n7).build();
        Node n4 = Node.builder().addChild(n5).build();
        Node node1 = Node.builder()
                .addChild(Node.builder().addChild(n4).build())
                .addChild(Node.builder().build())
                .addChild(Node.builder().build())
                .build();

        Node node2 = Node.builder()
                .addChild(Node.builder().build())
                .addChild(Node.builder().build())
                .build();

        return Node.builder()
                .addChild(node1)
                .addChild(node2)
                .build();
    }

    @GetMapping("/load-origin-tree")
    public Node loadOriginTree() {
        roots = new ArrayList<>();
        localCache = new HashMap<>();
        localCacheForNewNodes = new HashMap<>();
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
    public List<Node> nodeCopy(@RequestBody Node nodeToCopy) {

        if (nodeToCopy.getParentId() == null) {
            roots.add(nodeToCopy);
            return Collections.singletonList(nodeToCopy);
        }
        var result = new ArrayList<Node>();

        CopyResult copyResult = null;
        for (Node root : roots) {
            copyResult = recursiveSearchAndAdd(root, nodeToCopy);
            if (copyResult.getParentNode() != null) {
                if (copyResult.getParentNode().isDeleted()) {
                    copyResult.getChildNode().markAsDeleted();
                }
                result.add(copyResult.getChildNode());
                break;
            }
        }
        if (copyResult != null && copyResult.getParentNode() == null) {
            roots.add(nodeToCopy);
            result.add(copyResult.getChildNode());
        }

        Iterator<Node> iterator = roots.iterator();
        while (iterator.hasNext()) {
            Node item = iterator.next();
            if (nodeToCopy.getId().equals(item.getParentId())) {
                nodeToCopy.addNode(item);
                iterator.remove();
            }
        }

        if (result.isEmpty()) {
            roots.add(nodeToCopy);
            return Collections.singletonList(nodeToCopy);
        }

        return result;
    }

    @PostMapping("/node/copy/v2")
    public List<Node> nodeCopyV2(@RequestBody Node nodeToCopy) {

        localCache.put(nodeToCopy.getId(), nodeToCopy);

        return Collections.singletonList(nodeToCopy);
    }

    @DeleteMapping("/node")
    public Optional<Node> delete(@RequestBody Node nodeToDelete) {
//        Optional<Node> searchResult = Optional.empty();
//        for (Node root : roots) {
//            searchResult = recursiveSearch(root, nodeToDelete);
//            if (searchResult.isPresent()) {
//                searchResult.get().markAsDeleted();
//                break;
//            }
//        }
        localCache.get(nodeToDelete.getId()).markAsDeleted();

        return Optional.empty();
    }

    @PatchMapping("/node")
    public Optional<Node> patchNodeValue(@RequestBody Node nodeToPatch) {
//        Optional<Node> searchResult = Optional.empty();
//        for (Node root : roots) {
//            searchResult = recursiveSearch(root, nodeToPatch);
//            if (searchResult.isPresent()) {
//                searchResult.get().setValue(nodeToPatch.getValue());
//                break;
//            }
//        }

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

//        Optional<Node> searchResult = Optional.empty();
//        Node newNode = null;
//        for (Node root : roots) {
//            searchResult = recursiveSearch(root, Node.builder().withId(nodeToCreate.getParentId()).build());
//            if (searchResult.isPresent()) {
//                newNode = Node.builder()
//                        .withValue(nodeToCreate.getValue())
//                        .withParentId(searchResult.get().getId())
//                        .build();
//                searchResult.get().addNode(newNode);
//                break;
//            }
//        }
//
//        if (newNode == null) {
//            return new ResponseEntity(
//                    Optional.of(new ApiError("unknown error")),
//                    HttpStatus.INTERNAL_SERVER_ERROR
//            );
//        }

        return new ResponseEntity(Optional.of(newNode), HttpStatus.OK);
    }

    private CopyResult recursiveSearchAndAdd(Node currentTopNode, Node searchingNode) {
        if (currentTopNode.getId().equals(searchingNode.getParentId())) {
            currentTopNode.addNode(searchingNode);
            return new CopyResult(searchingNode, currentTopNode);
        }
        Optional<Node> medium = currentTopNode.getNodes().stream().filter(i -> i.getId().equals(searchingNode.getParentId())).findFirst();
        if (medium.isPresent()) {
            medium.get().addNode(searchingNode);
            return new CopyResult(searchingNode, medium.get());
        } else {
            for (Node childNode : currentTopNode.getNodes()) {
                var copyResult = recursiveSearchAndAdd(childNode, searchingNode);
                if (copyResult.getParentNode() != null) {
                    return copyResult;
                }
            }
        }

        return new CopyResult(searchingNode);
    }

    private Optional<Node> recursiveSearch(Node currentTopNode, Node searchingNode) {
        if (currentTopNode.getId().equals(searchingNode.getId())) {
            return Optional.of(currentTopNode);
        }
        if (currentTopNode.getNodes() == null || currentTopNode.getNodes().isEmpty()) {
            return Optional.empty();
        }
        Optional<Node> medium = currentTopNode.getNodes()
                .stream()
                .filter(i -> i.getId().equals(searchingNode.getId()) && !i.isDeleted())
                .findFirst();
        if (medium.isPresent()) {
            return medium;
        } else {
            for (Node childNode : currentTopNode.getNodes()) {
                Optional<Node> searchResult = recursiveSearch(childNode, searchingNode);
                if (searchResult.isPresent()) {
                    return searchResult;
                }
            }
        }

        return Optional.empty();
    }
}
