# simple-java-http-server

simple java server with backend call.

This branch, the app will create 1000 thread to call backend service, for each rest api call. To simulate buggy app behavior. Finally, it will OOM, because of too many threads.

# build

```bash

# on vultr
dnf install -y java-1.8.0-openjdk-devel java-11-openjdk-devel java-17-openjdk-devel java-21-openjdk-devel

dnf install -y /usr/bin/podman-compose /usr/bin/mvn


# checkout
mkdir -p /data
cd /data

git clone https://github.com/wangzheng422/simple-java-http-server


# build jar
cd /data/simple-java-http-server
git checkout threads

mvn clean package

# create container image for simple version, which will not copy javaagent.jar to the container
podman build --squash -t quay.io/wangzheng422/qimgs:simple-java-http-server-threads-2024.06.26.v02 -f Dockerfile.simple ./

podman push quay.io/wangzheng422/qimgs:simple-java-http-server-threads-2024.06.26.v02

# run with simple version
podman run -it --rm -p 8080:8080 \
    --name 'simple-java-http-server-threads' \
    -e WZH_URL="http://10.5.96.3:13000/file.txt" \
    -e JAVA_OPTS="-Dcom.sun.management.jmxremote.port=9091 -Dcom.sun.management.jmxremote.authenticate=false" \
    quay.io/wangzheng422/qimgs:simple-java-http-server-threads-2024.05.06.v01

# run with javaagent and collector, to see the result locally
podman-compose up --build


# on localhost, call the rest api to test
curl -vvv http://localhost:8080/sendRequest


```

## run multi-conn python backend

```bash
mkdir -p /data/py.test
cd /data/py.test

# dd if=/dev/urandom of=random_file.txt bs=100KB count=1

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