# simple-java-http-server
simple java server with backend call


# build

```bash

# checkout
mkdir -p /data
cd /data

git clone https://github.com/wangzheng422/simple-java-http-server


# build jar
cd /data/simple-java-http-server
mvn clean package


# create container image
podman build --squash -t quay.io/wangzheng422/qimgs:simple-java-http-server-2024.04.14 ./


# run 
podman-compose up --build


```