## Requirements

* Docker

## Build

Run next commands in the project root
```
docker build . -t artur-demo --no-cache --progress=plain
docker run -it -p80:8080 docker.io/library/artur-demo
```

## Use it

[http://localhost:80/]()
