# صورة جافا خفيفة (JDK 17 لأن مشروعك مكمبايل على 17)
FROM eclipse-temurin:17-jre

# نشتغل داخل /app
WORKDIR /app

# ننسخ ملفات Quarkus jar layout
COPY target/quarkus-app/lib/ ./lib/
COPY target/quarkus-app/*.jar ./
COPY target/quarkus-app/app/ ./app/
COPY target/quarkus-app/quarkus/ ./quarkus/

# نفس البورت اللي حاطينه في deployment (8080)
EXPOSE 8080

# نشغل Quarkus
ENTRYPOINT ["java","-jar","quarkus-run.jar"]
