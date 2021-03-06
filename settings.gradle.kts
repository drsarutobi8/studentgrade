/*
 * This file was generated by the Gradle 'init' task.
 */
pluginManagement {
    val quarkusPluginVersion: String by settings
    val quarkusPluginId: String by settings
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id(quarkusPluginId) version quarkusPluginVersion
        id("io.freefair.lombok") version "6.3.0"
        id("org.kordamp.gradle.jandex") version "0.11.0"
        id("com.palantir.docker-compose") version "0.32.0"
        //id("com.palantir.docker-run") version "0.27.0"
    }
}

rootProject.name = "studentgrade"
include("common","resultApi","resultService","studentApi","studentService")
