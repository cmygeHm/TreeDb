package com.example.demo.controller;

import com.example.demo.model.Node;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tree")
public class TreeController {

    private final List<Node> roots = new ArrayList<>();
    private Node root;

    @GetMapping("/load-origin-tree")
    public Node loadOriginTree() {

        Node node1 = Node.builder()
                .addChild(Node.builder().build())
                .addChild(Node.builder().build())
                .addChild(Node.builder().build())
                .build();

        Node node2 = Node.builder()
                .addChild(Node.builder().build())
                .addChild(Node.builder().build())
                .build();

        root = node1;

        return Node.builder()
                .withValue("root")
                .addChild(node1)
                .addChild(node2)
                .build();
    }

    @PostMapping("/node/copy")
    public Node nodeCopy(@RequestBody Node node) {
        if (node.getParentId() == null) {
            root = node;
        } else {
            if (root.getId() == node.getParentId()) {
                root.addNode(node);
            } else {
                Optional<Node> medium = root.getNodes().stream().filter(i -> i.getId() == node.getParentId()).findFirst();
                medium.ifPresent(value -> value.addNode(node));
            }
        }

        return root;
    }
}
