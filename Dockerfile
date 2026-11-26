# STAGE 1: Angular
FROM node:20-alpine AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build -- --configuration production

# STAGE 2: Spring Boot
FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-build
WORKDIR /app
COPY backend/pom.xml .
RUN mvn -q dependency:go-offline
COPY backend/src ./src
RUN mvn -q clean package -DskipTests

# STAGE 3: Final Image
FROM alpine:3.19
RUN apk add --no-cache openjdk21-jre-headless nginx postgresql16 postgresql16-client su-exec bash tzdata

COPY frontend/nginx.conf /etc/nginx/http.d/default.conf
COPY --from=frontend-build /app/dist/aliagri-frontend/browser /usr/share/nginx/html
COPY --from=backend-build /app/target/*.jar /app/app.jar
COPY entrypoint.sh /entrypoint.sh

# /run/postgresql = unix socket + lock dir. Postgres on Alpine REFUSES to start without it.
RUN chmod +x /entrypoint.sh \
    && mkdir -p /run/postgresql /run/nginx /var/lib/postgresql/data \
    && chown postgres:postgres /run/postgresql /var/lib/postgresql/data

ENV TZ=Africa/Casablanca \
    SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:5432/aliagri \
    SPRING_DATASOURCE_USERNAME=aliagri

VOLUME /var/lib/postgresql/data
EXPOSE 80
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -q --spider http://127.0.0.1/ || exit 1
ENTRYPOINT ["/entrypoint.sh"]