FROM amazoncorretto:21-alpine

# Устанавливаем curl для healthcheck
RUN apk add --no-cache curl

WORKDIR /app

# Копируем JAR файл
COPY target/main-server-*.jar app.jar

# Открываем порт
EXPOSE 8080

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]