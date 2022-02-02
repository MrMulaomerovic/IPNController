FROM openjdk:11-jre-slim
VOLUME /tmp
EXPOSE 8090
RUN mkdir -p /app
WORKDIR /app
ARG BUILD_VERSION=0.0.1-SNAPSHOT
ARG ENVIRONMENT='default'
ADD target/payment-${BUILD_VERSION}.jar app.jar
ENV PROFILE=${ENVIRONMENT}
ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar --spring.profiles.active=$PROFILE"]
