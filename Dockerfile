FROM node:23-alpine AS builder1
LABEL authors="jo yie"

WORKDIR /app

COPY /frontend .

RUN npm ci
RUN npm i -g @angular/cli
RUN ng build

FROM eclipse-temurin:23 AS builder2

WORKDIR /app

COPY /backend .
COPY --from=builder1 /app/dist/frontend/browser/ /app/src/main/resources/static

RUN ./mvnw install -DskipTests

FROM eclipse-temurin:23

WORKDIR /app

COPY --from=builder2 /app/target/*.jar app.jar

ENV PORT=8080

EXPOSE ${PORT}

ENTRYPOINT SERVER_PORT=${PORT} java -jar /app/app.jar -Dserver.port=${PORT}