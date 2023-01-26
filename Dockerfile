# Build
# Image
FROM amazoncorretto:11 as build
WORKDIR /root/application
COPY . .

EXPOSE 8081
CMD ./gradlew clean run