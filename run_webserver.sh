export MAVEN_OPTS=-XX:-UseSplitVerifier
(cd common ; mvn clean install -q )
(cd instrumentation ; mvn clean install -q)
(cd web-server ; mvn clean install -q )
(cd instrumentation ; mvn exec:java -q )
(cd web-server ; mvn exec:java -q)
