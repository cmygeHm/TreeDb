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

    function renderNode(data, parentElement, isRemoteTree) {
        if (data) {
            let ul = document.createElement("ul");
            let li = document.createElement("li");
            li.dataset.id = data.id
            if (isRemoteTree) {
                li.classList.add("remote");
            } else {
                li.classList.remove("remote");
                li.classList.add("local");
            }
            li.classList.add("list-element-base");
            if (data.deleted) {
                li.classList.add("list-element-deleted");
            }
            li.onclick = selectNode()
            li.textContent = data.value;
            ul.appendChild(li);
            parentElement.appendChild(ul);
            for (let i = 0; i < data.childNodes.length; i++) {
                renderNode(data.childNodes[i], ul, isRemoteTree);
            }
        }
    }

    function loadOriginTree() {
        selectedNode = null
        fetch("/tree/load-origin-tree",
            {
                method: "GET",
                headers:{"content-type":"application/json"}
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                let div = document.getElementById("remote-tree");
                div.innerHTML = "";
                for (let i = 0; i < data.length; i++) {
                    renderNode(data[i], div, true);
                }
            });
    }

    loadOriginTree();

    function applyTree() {
        fetch("/tree/apply",
            {
                method: "POST",
                headers:{"content-type":"application/json"}
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                let remoteDiv = document.getElementById("remote-tree");
                remoteDiv.innerHTML = "";
                for (let i = 0; i < data.db.length; i++) {
                    renderNode(data.db[i], remoteDiv, true);
                }
                let localDiv = document.getElementById("local-tree");
                localDiv.innerHTML = "";
                for (let i = 0; i < data.cache.length; i++) {
                    renderNode(data.cache[i], localDiv, false);
                }
            });
    }

    function renderLocalTree(data) {
        if (data.error) {
            alert(data.error);
            return false
        }
        let div = document.getElementById("local-tree");
        div.innerHTML = "";
        for (let i = 0; i < data.length; i++) {
            renderNode(data[i], div, false);
        }
    }

    function copyNode() {
        if (
            selectedNode == null
        ) {
            alert("Выберите ноду для копирования")
            return;
        }

        fetch(`/tree/node/${selectedNode.dataset.id}/copy`,
            {
                method: "POST",
                headers:{"content-type":"application/json"}
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                return renderLocalTree(data);
            });
    }

    function deleteNode(selectedNode) {
        if (selectedNode === null) {
            alert("Не выбрана нода для удаления")
            return false;
        }

        if (selectedNode.classList.contains("remote")) {
            alert("Скопируйте элемент в локальный кеш, чтобы работать с ним")
            return false;
        }

        fetch(`/tree/node/${selectedNode.dataset.id}`,
            {
                method: "DELETE",
                headers:{"content-type":"application/json"}
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                renderLocalTree(data);
            });
    }

    function renameNode() {
        if (selectedNode === null) {
            alert("Выберите элемент для редактирования")
            return false;
        }

        if (selectedNode.classList.contains("remote")) {
            alert("Скопируйте элемент в локальный кеш, чтобы работать с ним")
            return false;
        }

        let newNodeValue = prompt("Введите новое значение");

        if (newNodeValue == null) {
            return false;
        }

        fetch(`/tree/node/${selectedNode.dataset.id}`,
            {
                method: "PATCH",
                headers:{"content-type":"application/json"},
                body: JSON.stringify({value: newNodeValue})
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                renderLocalTree(data)
            });
    }

    function addNode() {
        if (
            selectedNode == null ||
            selectedNode.classList.contains("remote")
        ) {
            alert("Выделите элемент локальной БД")
            return false;
        }

        let newNodeValue = prompt("Введите значение для нового элемента");

        if (newNodeValue === null) {
            return false;
        }

        fetch(`/tree/node/${selectedNode.dataset.id}/add-child`,
            {
                method: "POST",
                headers:{"content-type":"application/json"},
                body: JSON.stringify({value: newNodeValue})
            })
            .then( response => {
                return response.json();
            })
            .then( data => {
                renderLocalTree(data)
            });
    }
}