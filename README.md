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
### Start Database Server (MariaDB 10.6.3) And Keycloak (14.0.0)

> ./gradlew :studentService:dockerComposeUp

Please confirm the following log message to make sure the server is successfully initialized (> 5 min on my machine)

```
docker ps
CONTAINER ID   IMAGE            COMMAND                  CREATED          STATUS          PORTS                                       NAMES
cc730644109a   mariadb:10.6.3   "docker-entrypoint.s…"   41 seconds ago   Up 34 seconds   0.0.0.0:3306->3306/tcp, :::3306->3306/tcp   mariadb

docker logs cc730644109a -f
```

> 2021-07-20 22:31:39 0 [Note] mysqld: ready for connections.

### Start Quarkus
> ./gradlew :studentService:quarkusDev

### Additional Keycloak Settings
* Regenerate Secret for client studentgrade-service and copy the value to below curl command
* Add user 'st1' (John Doe) with password 'st1' (with disabling temporary password) 
* Assign realm role 'user' and client role 'student'

*** REPLACE YOUR CLIENT SECRET ***


# To Test
## Using BloomRPC
* Result Server running at localhost port 9000
* Add TLS connection access by adding root certificate "resultService/src/main/resources/tls/ca.pem" and target "localhost"
* Student Server running at localhost port 9100
* Add TLS connection access by adding root certificate "studentService/src/main/resources/tls/ca.pem" and target "localhost"
* Run below command to get access token
```
export access_token=$(
curl -X POST http://localhost:8180/auth/realms/studentgrade-abc/protocol/openid-connect/token --user studentgrade-service:ea92ff27-0da7-4f71-aff2-63786c394033  -H 'content-type: application/x-www-form-urlencoded' -d 'username=st1&password=st1&grant_type=password' | jq --raw-output '.access_token'
);
echo $access_token;
curl -X POST \
  http://localhost:8180/auth/realms/studentgrade-abc/protocol/openid-connect/userinfo -H "Authorization: Bearer ${access_token}" 
```
* In Metadata section add following json:
{
    "authorization": "Bearer <*** REPLACE ACCESS TOKEN HERE ***>"
}