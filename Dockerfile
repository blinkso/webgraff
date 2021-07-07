# Build
# Image
FROM openjdk:8-jdk-alpine as build
WORKDIR /root/application
COPY . .

EXPOSE 8081
CMD ./gradlew clean run