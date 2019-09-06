FROM openjdk:8-alpine

COPY target/uberjar/mftickets.jar /mftickets/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/mftickets/app.jar"]
