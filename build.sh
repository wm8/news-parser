mvn clean
mvn dependency:copy-dependencies
mvn package -Dmaven.test.skip=true