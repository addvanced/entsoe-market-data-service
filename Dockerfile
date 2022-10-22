FROM openjdk:17
EXPOSE 80
COPY /build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]