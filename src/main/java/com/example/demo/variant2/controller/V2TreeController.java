package com.example.demo.variant2.controller;

import com.example.demo.variant2.model.ApiError;
import com.example.demo.variant2.model.CopyResult;
import com.example.demo.variant2.model.Node;
import com.example.demo.variant2.model.NodeDraft;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v2/tree")
public class V2TreeController {

    private List<Node> roots;

    @GetMapping("/load-origin-tree")
    public Node loadOriginTree() {

        roots = new ArrayList<>();
        Node root = Node.builder()
                .build();

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

        root.addNode(node1);
        root.addNode(node2);
        return root;
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

    @DeleteMapping("/node")
    public Optional<Node> delete(@RequestBody Node nodeToDelete) {
        Optional<Node> searchResult = Optional.empty();
        for (Node root : roots) {
            searchResult = recursiveSearch(root, nodeToDelete);
            if (searchResult.isPresent()) {
                searchResult.get().markAsDeleted();
                break;
            }
        }

        return searchResult;
    }

    @PatchMapping("/node")
    public Optional<Node> patch(@RequestBody Node nodeToPatch) {
        Optional<Node> searchResult = Optional.empty();
        for (Node root : roots) {
            searchResult = recursiveSearch(root, nodeToPatch);
            if (searchResult.isPresent()) {
                searchResult.get().setValue(nodeToPatch.getValue());
                break;
            }
        }

        return searchResult;
    }

    @PostMapping("/node")
    public ResponseEntity<Optional<Node>> post(@RequestBody NodeDraft nodeToCreate) {
        if (nodeToCreate.getParentId() == null) {
            return new ResponseEntity(
                    Optional.of(new ApiError("parentId required for create new node")),
                    HttpStatus.BAD_REQUEST
            );
        }
        Optional<Node> searchResult = Optional.empty();
        Node newNode = null;
        for (Node root : roots) {
            searchResult = recursiveSearch(root, Node.builder().withId(nodeToCreate.getParentId()).build());
            if (searchResult.isPresent()) {
                newNode = Node.builder()
                        .withValue(nodeToCreate.getValue())
                        .withParentId(searchResult.get().getId())
                        .build();
                searchResult.get().addNode(newNode);
                break;
            }
        }

        if (newNode == null) {
            return new ResponseEntity(
                    Optional.of(new ApiError("unknown error")),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

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
