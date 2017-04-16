cd $(mktemp -d) #go to new temporary dir
git clone git@github.com:miguel-amaral/CNV1617.git
cd CNV1617/
mvn clean install
cd web-server/
mvn exec:java
