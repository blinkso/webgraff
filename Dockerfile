# Build
# Image
FROM jdk-11.0.11_9-alpine as build
WORKDIR /root/application
COPY . .

EXPOSE 8081
CMD ./gradlew clean run