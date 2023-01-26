# Build
# Image
FROM 8-al2-jdk as build
WORKDIR /root/application
COPY . .

EXPOSE 8081
CMD ./gradlew clean run