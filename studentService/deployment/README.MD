# Docker Reference

## To Start Servers
* Start MariaDB Server (!!Wait until it complete initiated database before next step)
> docker-compose -f docker-compose-db.yml up

* Start Keycloak Server (!!Wait unit it complete initialized database about 10 minutes)
> docker-compose -f docker-compose-auth.yml up

## References
[Keycloak SSL setup using docker image](https://newbedev.com/keycloak-ssl-setup-using-docker-image)

* Generate a self-signed cert using the keytool
> keytool -genkey -alias localhost -keyalg RSA -keystore keycloak.jks -validity 10950

* convert .jks to .p12
> keytool -importkeystore -srckeystore keycloak.jks -destkeystore keycloak.p12 -deststoretype PKCS12

* generate .crt from .p12 keystore
> openssl pkcs12 -in keycloak.p12 -nokeys -out tls.crt

* generate .key from .p12 keystore
> openssl pkcs12 -in keycloak.p12 -nocerts -nodes -out tls.key

* Then use the tls.crt and tls.key for volume mount /etc/x509/https


## Useful Manual Docker Commands
* To create network
> docker network create studentgrade-network

* To start database
> docker run --net studentgrade-network --name keycloak-db -e MARIADB_ROOT_PASSWORD=keycloak-db -e MYSQL_DATABASE=keycloak-db -e MYSQL_USER=keycloak-db -e MYSQL_PASSWORD=keycloak-db -d mariadb:latest

* To test mysql from another client in same network
> docker run -it --network studentgrade-network --rm mariadb mysql -hkeycloak-db -ukeycloak-db -pkeycloak-db keycloak-db

* To start Keycloak with MariaDB
> docker run --net studentgrade-network --name keycloak-app -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -e KEYCLOAK_HTTP_PORT=8080 -e KEYCLOAK_HTTPS_PORT=8443 -e KEYCLOAK_IMPORT=/data/studentgrade-abc.json,/data/studentgrade-xyz.json -e DB_VENDOR=mariadb -e DB_ADDR=keycloak-db -e DB_DATABASE=keycloak-db -e DB_USER=keycloak-db -e DB_PASSWORD=keycloak-db  -d figker/figker-keycloak:1.0.0-SNAPSHOT

* or To start Keycloak with H2 DB
> docker run --net studentgrade-network --name keycloak-app -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -e KEYCLOAK_HTTP_PORT=8080 -e KEYCLOAK_HTTPS_PORT=8443 -e KEYCLOAK_IMPORT=/data/studentgrade-abc.json,/data/studentgrade-xyz.json   -d jboss/keycloak:latest 

* To get into shell of server
> docker run -it keycloak-app /bin/sh

* To see logs
> docker logs keycloak-app -f

> docker stop keycloak-app

> docker stop keycloak-db

> docker rm keycloak-app

> docker rm keycloak-db

* To remove network
> docker network rm studentgrade-network