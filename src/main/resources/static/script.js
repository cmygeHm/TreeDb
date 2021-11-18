document.addEventListener("DOMContentLoaded", ready)

function ready() {
    let selectedNode = null;

    document.getElementById("resetButton").onclick = function () {
        document.getElementById("local-tree").innerHTML = "";
        loadOriginTree();
    }

    document.getElementById("applyButton").onclick = function () {
        applyTree();
    }

    document.getElementById("copyNodeButton").onclick = function () {
        copyNode();
    }

    document.getElementById("addNodeButton").onclick = function () {
        addNode();
    }

    document.getElementById("deleteNodeButton").onclick = function () {
        if (selectedNode) {
            deleteNode(selectedNode);
        }
    }

    document.getElementById("editNodeButton").onclick = function () {
        renameNode();
    }

    function selectNode() {
        return function () {
            if (selectedNode) {
                selectedNode.classList.remove("list-element-selected");
            }
            this.classList.add("list-element-selected");
            selectedNode = this;
        };
    }

    function renderNode(data, parentElement, parents) {
        if (data) {
            let ul = document.createElement("ul");
            let li = document.createElement("li");
            li.dataset.id = data.id
            li.classList.add("list-element-base", "remote");
            if (data.deleted) {
                li.classList.add("list-element-deleted");
            }
            li.onclick = selectNode()
            if (data.parentId !== null) {
                li.dataset.parentId = data.parentId;
            }
            if (parents == null) {
                li.dataset.parents = data.id;
            } else {
                li.dataset.parents = parents + " " + data.id;
            }
            li.textContent = data.value;
            ul.appendChild(li);
            parentElement.appendChild(ul);
            for (let i = 0; i < data.nodes.length; i++) {
                renderNode(data.nodes[i], ul, li.dataset.parents);
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

    function applyTree() {
        fetch("/v2/tree/apply",
            {
                method: "POST",
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

    function copyNode() {
        if (
            selectedNode == null ||
            selectedNode.classList.contains("local")
        ) {
            return;
        }

        let id = parseInt(selectedNode.dataset.id);
        if (document.querySelector('.local[data-id="' + id + '"]')) {
            console.log("already cached")
            return;
        }

        let postBody = {id: id, value: selectedNode.textContent, nodes: []}
        if (selectedNode.dataset.parentId !== null) {
            postBody.parentId = parseInt(selectedNode.dataset.parentId);
        }

        function deepCopy(aloneElements) {
            for (let alone of aloneElements) {
                if (document.querySelector('.local[data-id="' + alone.dataset.parentId + '"] ~ ul') == null) {
                    document.querySelector('.local[data-id="' + alone.dataset.parentId + '"]').after(document.createElement("ul"));
                }
                document.querySelector('.local[data-id="' + alone.dataset.parentId + '"]').nextSibling.append(alone);

                let aloneNodes = document.querySelectorAll('.local[data-parent-id="' + alone.dataset.id + '"]');
                deepCopy(aloneNodes);
            }
        }

        function processCopiedNode(copyResult) {
            let childNodeId = copyResult.id;
            let remoteToCopy = document.querySelector('.remote[data-id="' + childNodeId + '"]');
            let copiedNode = remoteToCopy.cloneNode(true);
            copiedNode.classList.remove("list-element-selected", "remote");
            copiedNode.classList.add("local");
            copiedNode.onclick = selectNode()
            if (
                copyResult.parentId === null ||
                document.querySelector('.local[data-id="' + copyResult.parentId + '"]') == null
            ) {
                let copyDiv = document.getElementById("local-tree");
                let ul = document.createElement("ul");
                ul.append(copiedNode);
                copyDiv.appendChild(ul);
                copyDiv.appendChild(document.createElement("ul"));
            } else {
                if (document.querySelector('.local[data-id="' + copyResult.parentId + '"] ~ ul') == null) {
                    document.querySelector('.local[data-id="' + copyResult.parentId + '"]').after(document.createElement("ul"));
                }
                document.querySelector('.local[data-id="' + copyResult.parentId + '"]').nextSibling.append(copiedNode);
            }
            let aloneNodes = document.querySelectorAll('.local[data-parent-id="' + copiedNode.dataset.id + '"]');
            deepCopy(aloneNodes);
            if (copyResult.deleted) {
                deleteNode(copiedNode)
            }
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

    function deleteNode(selectedNode) {
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
                return true;
            })
            .then( data => {
                document.querySelector('.local[data-id="' + selectedNode.dataset.id + '"]').classList.add("list-element-deleted")
                document.querySelectorAll('.local[data-parents~="' + selectedNode.dataset.id + '"]')
                    .forEach(function (element) {
                        element.classList.add("list-element-deleted")
                    })
            });
    }

    function renameNode() {
        if (selectedNode === null) {
            return false;
        }
        if (selectedNode.classList.contains("list-element-deleted")) {
            alert("Нельзя добавить к удаленному элементу")
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
                document.querySelector('.local[data-id="' + selectedNode.dataset.id + '"]').textContent = newNodeValue
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
        if (selectedNode.classList.contains("list-element-deleted")) {
            alert("Нельзя добавить к удаленному элементу")
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
                if (document.querySelector('.local[data-id="' + data.parentId + '"] + ul') === null) {
                    document.querySelector('.local[data-id="' + data.parentId + '"]').after(document.createElement("ul"));
                }
                let newElement = document.createElement("li")
                newElement.textContent = data.value;
                newElement.dataset.id = data.id;
                newElement.dataset.parentId = data.parentId;
                newElement.classList.add("list-element-base", "local")
                newElement.onclick = selectNode()
                document.querySelector('.local[data-id="' + data.parentId + '"] + ul').append(newElement);
            });
    }
}