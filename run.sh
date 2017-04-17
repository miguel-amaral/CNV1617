cd $(mktemp -d) #go to new temporary dir
mvn clean install
cd Instrumentation/src/
export CLASSPATH=$CLASSPATH:$(pwd)
cd ../../web-server/
mvn exec:java
