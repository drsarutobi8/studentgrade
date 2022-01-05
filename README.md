# studentgrade
This project will implement the example of 

[How To Create gRPC Microservices with JPA.](https://medium.com/geekculture/how-to-create-grpc-microservices-with-jpa-b3e804b4d91e) By Hashan Mahesh 

and 

[Securing Java gRPC services with JWT-based authentication](https://sultanov.dev/blog/securing-java-grpc-services-with-jwt-based-authentication/)


Implementing Quarkus 2.0.3, MariaDB 10.6.3, Keycloak 14.0.0, and Gradle Kotlin 7.0.2

Using Following Quarkus Technology
* Quarkus Grpc Service and Client (Enabling TLS)
* Quarkus Hibernate Reactive Panache and Hibernate ORM Panache

# To Start Servers
## Start Result Server 
Result Service uses H2 as in-memory Database server, Hibernate ORM Panache (non-reactive) and blocking stub. 
### Start Quarkus 
> ./gradlew :resultService:quarkusDev

## Start Student Server
Student Service uses MariaDB Database Server, Hibernate Reactive Panache and non-blocking stub.
 
### Install docker
[Install docker on fedora](https://docs.docker.com/engine/install/fedora/)

* Add user to docker group
```
sudo usermod -aG docker <YOUR USER>
```

* Give permission to access docker socket
```
sudo chmod 666 /var/run/docker.sock
```

* Install docker-compose
```shell
sudo dnf install docker-compose
sudo chmod +x /usr/bin/docker-compose
```

### Start Database Server (MariaDB)

```shell
cd studentService
docker-compose -f docker-compose-db.yml up -d
```
* Please confirm the following log message to make sure the server is successfully initialized (> 5 min on my machine)

```
docker ps
CONTAINER ID   IMAGE            COMMAND                  CREATED          STATUS          PORTS                                       NAMES
cc730644109a   mariadb:10.6.3   "docker-entrypoint.s…"   41 seconds ago   Up 34 seconds   0.0.0.0:3306->3306/tcp, :::3306->3306/tcp   mariadb

docker logs cc730644109a -f
```
* looking for something like:
> 2021-07-20 22:31:39 0 [Note] mysqld: ready for connections.

### Start Identity Server (Keycloak)
**You should use pre-installed server [auth.figker.com](https://auth.figker.com) as default Identity Server since the mobile client requires valid certificate**

In the case you want to try on localhost:

```shell
cd studentService
docker-compose -f docker-compose-auth.yml up -d
```
* Using 'docker ps' and 'docker logs' as above in database installation to see the activities.
  * Keycloak will take time to initialize database (my machine > 10 minutes)

* Additional Keycloak Settings
  * Change access type of client studentgrade-service to 'public'
  * Add user 'st1' (John Doe) with password 'st1' (with disabling temporary password) 
  * Assign realm role 'user' and client role 'student'

### Start Quarkus
```
./gradlew :studentService:quarkusDev
```

### To run Envoy Proxy Server (for GRPC-Web)
* Check your current ip
```
ifconfig
```
* Copy ip address and replace in /studentService/deployment/proxy/envoy.yaml
```
load_assignment:
      cluster_name: cluster_0
      endpoints:
        - lb_endpoints:
            - endpoint:
                address:
                  socket_address:
                    address: 10.108.139.137 #change your address here (ifconfig)
                    port_value: 9200
```
* Run server
```
docker run -p 9280:9280 -v $(pwd)/studentService/deployment/proxy/envoy.yaml:/etc/envoy/envoy.yaml -e ENVOY_UID=$(id -u) envoyproxy/envoy:v1.17.0
```

# To Test
## Get access token
* Install jq to read access token
```
sudo dnf install jq
```
* Run below command to get access token
```
export access_token=$(
curl -X POST https://auth.figker.com:8443/auth/realms/studentgrade-abc/protocol/openid-connect/token --user studentgrade-service:ea92ff27-0da7-4f71-aff2-63786c394033  -H 'content-type: application/x-www-form-urlencoded' -d 'username=st1&password=st1&grant_type=password' | jq --raw-output '.access_token'
);
echo $access_token;
```
* To test if the access token is valid run
```
curl -X POST \
  https://auth.figker.com:8443/auth/realms/studentgrade-abc/protocol/openid-connect/userinfo -H "Authorization: Bearer ${access_token}" 
```

## Using BloomRPC
* Download the AppImage from [BloomRPC Release Page](https://github.com/bloomrpc/bloomrpc/releases)
* Change perssions to execute
```
chmod 555 ~/Downloads/BloomRPC-1.5.3.AppImage
```

* Run BloomRPC at Download folder
```
~/Download/BloomRPC-1.5.3.AppImage
```
* For Result Server, and Student Server (non-proxy) , add TLS connection access by adding root certificate "resultService/src/main/resources/tls/ca.pem" and target "auth.figker.com"
* For Student Server (proxy), disable TLS and use root certificate. Change option to Web (instead of GRPC)
* Add proto files 
  * For resultService adding resultApi/src/main/proto/result.proto
  * For studentService adding studentApi/src/main/proto/student.proto
* Change the address to testing server
  * Result Server running at 0.0.0.0:9000
  * Student Server running at 0.0.0.0:9200
  * Student Proxy Server running at 0.0.0.0:9280
* In Metadata section add following json:
```
{
    "authorization": "Bearer <*** REPLACE ACCESS TOKEN HERE ***>"
}
```