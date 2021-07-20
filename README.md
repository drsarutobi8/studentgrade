# studentgrade
This project will implement the example of [How To Create gRPC Microservices with JPA.](https://medium.com/geekculture/how-to-create-grpc-microservices-with-jpa-b3e804b4d91e) By Hashan Mahesh implementing Quarkus 2.0.2 and Gradle Kotlin 7.0.2

Using Following Quarkus Technology
* Quarkus Grpc Service and Client
* Quarkus Hibernate Reactive Panache

# To Start Servers
## Start Result Server 
### Start Quarkus (Using H2 as in-memory Database server)
> ./gradlew :resultService:quarkusDev

## Start Student Server
### Start Database Server (MariaDB 10.6.3)

> ./gradlew :studentServer:dockerRun

Please make sure you see the log as following message to make sure the server is successfully initialized (> 5 min on my machine)

> 2021-07-20 22:31:39 0 [Note] mysqld: ready for connections.

### Start Quarkus
> ./gradlew :studentService:quarkusDev

# To Test
## Using BloomRPC
* Result Server at port 9000
* Student Server at port 9100
