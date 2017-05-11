echo "Its a brave new world!"
mvn clean install -q && mvn -pl loadbalancer exec:java -q
