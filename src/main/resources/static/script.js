document.addEventListener("DOMContentLoaded", ready)

function ready() {
    let selectedNode;

    document.getElementById("copyNodeButton").onclick = function () {
        copyNode();
    }

    function selectNode(li) {
        return function () {
            if (selectedNode) {
                selectedNode.classList.remove("list-element-click");
            }
            li.classList.add("list-element-click");
            selectedNode = li;
        };
    }

    function renderNode(data, parentElement) {
        if (data) {
            var ul = document.createElement("ul");
            var li = document.createElement("li");
            li.setAttribute("id", "node" + data.id);
            li.setAttribute("data-id", data.id);
            li.classList.add("list-element-base");
            li.onclick = selectNode(li)
            if (data.parentId !== null) {
                li.setAttribute("data-parent-id", data.parentId);
            }
            li.appendChild(document.createTextNode(data.value));
            ul.appendChild(li);
            parentElement.appendChild(ul);
            for (let i = 0; i < data.nodes.length; i++) {
                renderNode(data.nodes[i], ul);
            }
        }
    }

    function loadOriginTree() {
        fetch("/v2/tree/load-origin-tree",
            {
                method: "GET",
                headers:{"content-type":"application/json"}
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                var div = document.getElementById("remote-tree");
                div.innerHTML = "";
                renderNode(data, div);
            });
    }
    loadOriginTree();

    function copyNode() {
        if (selectedNode == null || selectedNode.classList.contains("list-element-copied")) {
            return;
        }
        let id = parseInt(selectedNode.dataset.id);
        let postBody = {id: id, value: selectedNode.textContent, nodes: []}
        if (selectedNode.dataset.parentId !== null) {
            postBody.parentId = parseInt(selectedNode.dataset.parentId);
        }

        function deepCopy(aloneElements) {
            for (let alone of aloneElements) {
                if (document.querySelector("#local-node" + alone.dataset.parentId).nextSibling === null) {
                    document.querySelector("#local-node" + alone.dataset.parentId).after(document.createElement("ul"));
                }
                document.querySelector("#local-node" + alone.dataset.parentId).nextSibling.append(alone);

                let aloneNodes = document.querySelectorAll('.local[data-parent-id="' + alone.dataset.id + '"]');
                deepCopy(aloneNodes);
            }
        }

        function processCopiedNode(copyResult) {
            let childNodeId = copyResult.childNode.id;
            let remoteToCopy = document.getElementById("node" + childNodeId);
            remoteToCopy.classList.add("list-element-copied")
            let copiedNode = remoteToCopy.cloneNode(true);
            copiedNode.setAttribute("id", "local-" + copiedNode.getAttribute("id"))
            copiedNode.classList.remove("list-element-click");
            copiedNode.classList.add("local");
            copiedNode.onclick = selectNode(copiedNode)
            if (copyResult.parentNode === null) {
                let copyDiv = document.getElementById("local-tree");
                copyDiv.append(copiedNode);
                copyDiv.appendChild(document.createElement("ul"));
            } else {
                if (document.querySelector("#local-node" + copyResult.parentNode.id).nextSibling === null
                ) {
                    document.querySelector("#local-node" + copyResult.parentNode.id).after(document.createElement("ul"));
                }
                document.querySelector("#local-node" + copyResult.parentNode.id).nextSibling.append(copiedNode);
            }
            let aloneNodes = document.querySelectorAll('.local[data-parent-id="' + copiedNode.dataset.id + '"]');
            deepCopy(aloneNodes);
        }

        fetch("/v2/tree/node/copy",
            {
                method: "POST",
                headers:{"content-type":"application/json"},
                body: JSON.stringify(postBody)
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                for (let i = 0; i <data.length; i++) {
                    processCopiedNode(data[i]);
                }
            });
    }
}