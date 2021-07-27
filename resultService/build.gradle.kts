/*
 * This file was generated by the Gradle 'init' task.
 */

plugins {
    java
    `maven-publish`
    id("io.quarkus")
    id("io.freefair.lombok") 
    id("org.kordamp.gradle.jandex")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.maven.apache.org/maven2/")
    }

    maven {
        url = uri("https://maven.pkg.github.com/drsarutobi8/*")
    }
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(project(":common"))
    implementation(project(":resultApi"))
    compileOnly("org.projectlombok:lombok:1.18.20")

    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-grpc")
    implementation("io.quarkus:quarkus-hibernate-orm-panache")
    implementation("io.quarkus:quarkus-jdbc-h2")
    implementation("io.quarkus:quarkus-resteasy")
    implementation("io.quarkus:quarkus-keycloak-authorization")
    implementation("io.quarkus:quarkus-oidc")
    implementation("io.rest-assured:rest-assured")

    runtimeOnly("io.quarkus:quarkus-hibernate-orm-deployment")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("org.awaitility:awaitility")
    testImplementation("org.assertj:assertj-core")
}

group = "org.acme"
version = "1.0.0-SNAPSHOT"
description = "grpc-plain-text-quickstart"
java.sourceCompatibility = JavaVersion.VERSION_11

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
