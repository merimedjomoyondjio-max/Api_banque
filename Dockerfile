FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

ENV PORT=8080

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "if [ -n \"$SPRING_DATASOURCE_URL\" ]; then case \"$SPRING_DATASOURCE_URL\" in jdbc:*) ;; *) export SPRING_DATASOURCE_URL=\"jdbc:$SPRING_DATASOURCE_URL\" ;; esac; elif [ -n \"$DATABASE_URL\" ]; then case \"$DATABASE_URL\" in jdbc:*) export SPRING_DATASOURCE_URL=\"$DATABASE_URL\" ;; *) export SPRING_DATASOURCE_URL=\"jdbc:$DATABASE_URL\" ;; esac; fi; exec java -Dserver.port=${PORT:-8080} $JAVA_TOOL_OPTIONS -jar /app/app.jar"]
