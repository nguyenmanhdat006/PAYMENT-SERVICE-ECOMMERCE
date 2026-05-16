FROM eclipse-temurin:17-jdk-alpine AS builder

COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp clean package -DskipTests \
    && find target -maxdepth 1 -type f -name "*.jar" ! -name "*.original" -exec cp {} /workspace/app.jar \;

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -q

COPY src ./src
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/paymentservice-0.0.1-SNAPSHOT.jar payment-service.jar

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/payment_db
ENV SPRING_DATASOURCE_USERNAME=nguyendat
ENV SPRING_DATASOURCE_PASSWORD=nguyendat
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update
ENV SPRING_JPA_SHOW_SQL=false

ENV SERVER_PORT=8085
ENV SERVER_SERVLET_CONTEXT_PATH=/api
ENV SPRING_APPLICATION_NAME=payment-service

ENV VNPAY_TMN_CODE=DEMO
ENV VNPAY_HASH_SECRET=DEMOHASHSECRET
ENV VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
ENV VNPAY_RETURN_URL=http://localhost:8085/api/payments/vnpay/callback
ENV VNPAY_VERSION=2.1.0
ENV VNPAY_COMMAND=pay

ENV ORDER_SERVICE_URL=http://localhost:8084
ENV FRONTEND_URL=http://localhost:3000

ENV LOGGING_LEVEL_COM_ECOMMERCE=INFO
ENV LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=INFO
ENV LOGGING_LEVEL_ORG_HIBERNATE=WARN

EXPOSE 8085

USER appuser

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "payment-service.jar"]
