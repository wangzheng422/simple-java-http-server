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


# create container image for simple version, which will not copy javaagent.jar to the container
podman build --squash -t quay.io/wangzheng422/qimgs:simple-java-http-server-2024.04.24 -f Dockerfile.simple ./

podman push quay.io/wangzheng422/qimgs:simple-java-http-server-2024.04.24

# run with javaagent and collector, to see the result locally
podman-compose up --build


# on localhost, call the rest api to test
curl -vvv http://localhost:8080/sendRequest


```

## run multi-conn python backend

```bash

mkdir -p /data/py.test
cd /data/py.test

# create random ascii text file, with 100kb size
cat /dev/urandom | tr -dc 'A-Za-z0-9' | head -c $((100*1024)) > file.txt
echo >> file.txt
echo >> file.txt

cat << EOF > httpdemo.py
from http.server import ThreadingHTTPServer, SimpleHTTPRequestHandler

def run(server_class=ThreadingHTTPServer, handler_class=SimpleHTTPRequestHandler):
    server_address = ('', 13000)
    httpd = server_class(server_address, handler_class)
    httpd.serve_forever()

if __name__ == '__main__':
    run()
EOF

python3 httpdemo.py

```