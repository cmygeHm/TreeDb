package com.example.demo.variant2.controller;

import com.example.demo.variant2.model.CopyResult;
import com.example.demo.variant2.model.Node;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public List<CopyResult> nodeCopy(@RequestBody Node nodeToCopy) {

        if (nodeToCopy.getParentId() == null) {
            roots.add(nodeToCopy);
            return Collections.singletonList(new CopyResult(nodeToCopy));
        }
        var result = new ArrayList<CopyResult>();

        CopyResult copyResult = null;
        for (Node root : roots) {
            copyResult = recursiveSearch(root, nodeToCopy);
            if (copyResult.getParentNode() != null) {
                result.add(copyResult);
                break;
            }
        }
        if (copyResult != null && copyResult.getParentNode() == null) {
            roots.add(nodeToCopy);
            result.add(copyResult);
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
            return Collections.singletonList(new CopyResult(nodeToCopy));
        }

        return result;
    }

    private CopyResult recursiveSearch(Node currentTopNode, Node searchingNode) {
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
                var copyResult = recursiveSearch(childNode, searchingNode);
                if (copyResult.getParentNode() != null) {
                    return copyResult;
                }
            }
        }

        return new CopyResult(searchingNode);
    }
}
