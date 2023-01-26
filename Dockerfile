# Build
# Image
FROM 21-jdk as build
WORKDIR /root/application
COPY . .

EXPOSE 8081
CMD ./gradlew clean run