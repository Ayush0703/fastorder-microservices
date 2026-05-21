# Purani line: FROM eclipse-temurin:23-jdk-jammy
# Nayi line (Ise use karein):
FROM eclipse-temurin:23-jdk

WORKDIR /app

# Screenshot ke hisaab se JAR copy karein
COPY target/inventory-service-1.0-SNAPSHOT.jar app.jar

# Inventory service port
EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]
