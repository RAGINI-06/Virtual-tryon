FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

RUN mkdir -p /app/uploads

EXPOSE 8080

CMD ["sh", "-c", "java -jar target/*.jar"]