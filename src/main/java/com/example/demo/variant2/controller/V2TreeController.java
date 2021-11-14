package com.example.demo.variant2.controller;

import com.example.demo.variant2.model.CopyResult;
import com.example.demo.variant2.model.Node;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/v2/tree")
public class V2TreeController {

    private HashMap<Long, Node> aloneNodes;
    private Node root;

    @GetMapping("/load-origin-tree")
    public Node loadOriginTree() {

        aloneNodes = new HashMap<>();
        root = Node.builder()
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
            root = nodeToCopy;
            return Collections.singletonList(new CopyResult(nodeToCopy));
        }
        var result = new ArrayList<CopyResult>(aloneNodes.size() + 1);
        CopyResult copyResult = recursiveSearch(root, nodeToCopy);
        result.add(copyResult);
        for(Map.Entry<Long, Node> aloneNode: aloneNodes.entrySet()) {
            CopyResult findResult = recursiveSearch(root, aloneNode.getValue());
            if (findResult.getParentNode() != null) {
                aloneNodes.remove(aloneNode.getKey());
            }
            result.add(findResult);
        }
        if (copyResult.getParentNode() == null) {
            aloneNodes.put(nodeToCopy.getId(), nodeToCopy);
        }

        return result;
    }

    private CopyResult recursiveSearch(Node currentTopNode, Node searchingNode) {
        if (currentTopNode.getId() == searchingNode.getParentId()) {
            currentTopNode.addNode(searchingNode);
            return new CopyResult(searchingNode, currentTopNode);
        }
        Optional<Node> medium = currentTopNode.getNodes().stream().filter(i -> i.getId() == searchingNode.getParentId()).findFirst();
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
