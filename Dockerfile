# Build
# Image
FROM 8u362-alpine3.17-jre as build
WORKDIR /root/application
COPY . .

EXPOSE 8081
CMD ./gradlew clean run