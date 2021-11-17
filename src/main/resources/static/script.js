document.addEventListener("DOMContentLoaded", ready)

function ready() {
    let selectedNode;

    document.getElementById("copyNodeButton").onclick = function () {
        copyNode();
    }

    document.getElementById("addNodeButton").onclick = function () {
        addNode();
    }

    document.getElementById("deleteNodeButton").onclick = function () {
        deleteNode();
    }

    document.getElementById("editNodeButton").onclick = function () {
        renameNode();
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
            let childNodeId = copyResult.id;
            let remoteToCopy = document.getElementById("node" + childNodeId);
            remoteToCopy.classList.add("list-element-copied")
            let copiedNode = remoteToCopy.cloneNode(true);
            copiedNode.setAttribute("id", "local-" + copiedNode.getAttribute("id"))
            copiedNode.classList.remove("list-element-click");
            copiedNode.classList.add("local");
            copiedNode.onclick = selectNode(copiedNode)
            if (
                copyResult.parentId === null ||
                document.querySelector("#local-node" + copyResult.parentId) === null
            ) {
                let copyDiv = document.getElementById("local-tree");
                copyDiv.append(copiedNode);
                copyDiv.appendChild(document.createElement("ul"));
            } else {
                if (document.querySelector("#local-node" + copyResult.parentId).nextSibling === null) {
                    document.querySelector("#local-node" + copyResult.parentId).after(document.createElement("ul"));
                }
                document.querySelector("#local-node" + copyResult.parentId).nextSibling.append(copiedNode);
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

    function deleteNode() {
        if (selectedNode === null) {
            return false;
        }

        let postBody = {
            id: selectedNode.dataset.id
        }

        fetch("/v2/tree/node",
            {
                method: "DELETE",
                headers:{"content-type":"application/json"},
                body: JSON.stringify(postBody)
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                document.querySelector("#local-node" + selectedNode.dataset.id).classList.add("list-element-deleted")
                document.querySelectorAll("#local-node" + selectedNode.dataset.id + " ~ ul li.list-element-base").forEach(function(element){
                    element.classList.add("list-element-deleted")
                })
            });
    }

    function renameNode() {
        if (selectedNode === null) {
            return false;
        }

        let newNodeValue = prompt("Введите новое значение");

        if (newNodeValue == null) {
            return false;
        }

        let postBody = {
            id: selectedNode.dataset.id,
            value: newNodeValue
        }

        fetch("/v2/tree/node",
            {
                method: "PATCH",
                headers:{"content-type":"application/json"},
                body: JSON.stringify(postBody)
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                document.querySelector("#local-node" + selectedNode.dataset.id).textContent = newNodeValue
            });
    }

    function addNode() {
        if (
            selectedNode == null ||
            !selectedNode.classList.contains("local")
        ) {
            alert("Выделите элемент локальной БД")
            return false;
        }

        let newNodeValue = prompt("Введите значение для нового элемента");

        if (newNodeValue === null) {
            return false;
        }

        let postBody = {
            parentId: selectedNode.dataset.id,
            value: newNodeValue
        }

        fetch("/v2/tree/node",
            {
                method: "POST",
                headers:{"content-type":"application/json"},
                body: JSON.stringify(postBody)
            })
            .then( response => {
                if (response.ok) {
                    return response.json();
                }

                console.log(response)
            })
            .then( data => {
                if (document.querySelector('.local[data-id="' + data.parentId + '"]').nextSibling == null) {
                    document.querySelector('.local[data-id="' + data.parentId + '"]').after(document.createElement("ul"));
                }
                let newElement = document.createElement("li")
                newElement.textContent = data.value;
                newElement.dataset.id = data.id;
                newElement.dataset.parentId = data.parentId;
                newElement.classList.add("list-element-base", "list-element-copied", "local")
                newElement.onclick = selectNode(newElement)
                document.createElement("ul").dataset.parentId = data.parentId;
                document.querySelector('.local[data-id="' + data.parentId + '"]').nextSibling.append(newElement);
            });
    }
}