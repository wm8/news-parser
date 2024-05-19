FROM maven:3.8.3-openjdk-17
ADD ../src src
ADD ../pom.xml pom.xml
RUN mvn clean
RUN mvn dependency:copy-dependencies
RUN mvn package -Dmaven.test.skip=true
ENTRYPOINT java -jar target/news-parser-1.0-SNAPSHOT.jar
