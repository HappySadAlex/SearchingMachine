FROM openjdk:17-jdk-alpine

MAINTAINER HappySadAlex <alex.drevilo@gmail.com>
LABEL version="1" authors="HappySadAlex", name="SearchingMachine"

WORKDIR /app
COPY target/SearchEngine-1.0-SNAPSHOT-spring-boot.jar /app/SearchEngine-1.0-SNAPSHOT-spring-boot.jar
ENTRYPOINT ["java", "-jar", "SearchEngine-1.0-SNAPSHOT-spring-boot.jar"]
