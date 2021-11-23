## Description

У задачи есть два варианта решения.
Одно находится в ветке master. В нем в БД каждая нода хранит ИДы всех парентов и при запросе этой ноды из БД, в ответ получаем только те ИДы парентов, которые содержатся в локальном кеше.
Второе решение находится в ветке https://github.com/cmygeHm/TreeDb/tree/tree_cache.
В нем мы на стороне БД помимо "таблицы" с записями Record, имеем дерево связей всех записей. Благодаря чему нам не нужно у каждой ноды хранить ИДы всех парентов.
Это дерево всегда соответствует состоянию БД.

## Requirements

* Docker

## Build

Run next commands in the project root
```
docker build . -t artur-demo --no-cache --progress=plain
docker run -it -p80:8080 docker.io/library/artur-demo
```

Another way to run:

```
./gradlew bootBuildImage --imageName=demo/artur:latest
docker run -p 80:8080 docker.io/demo/artur:latest
```

## Use it

[http://localhost:80/]()
