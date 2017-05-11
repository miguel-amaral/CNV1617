#!/usr/bin/env bash
#mvn clean install
#
################ BY RAFA ###############
######### PROCEED WITH CAUTION #########
########################################
#
#export PROJ_DIR=$(pwd)
##export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS
#
#cd instrumentation/src/
#javac $(find . -name "*.java")
#original= $CLASSPATH
#
#export CLASSPATH=$(pwd):$(pwd)/tool:./
#java -XX:-UseSplitVerifier InstTool $PROJ_DIR/web-server/target/classes/raytracer $PROJ_DIR/web-server/target/classes/raytracer
#cd ../../web-server/
#
#
########################################
######### -------------------- #########
########################################
#
## mvn exec:java
#export CLASSPATH=$CLASSPATH:$(pwd)/target/classes/
#java -XX:-UseSplitVerifier pt.tecnico.cnv.webserver.WebServer
#export CLASSPATH= $original

export MAVEN_OPTS=-XX:-UseSplitVerifier
(cd common ; mvn clean install -q )
(cd instrumentation ; mvn clean install -q)
(cd web-server ; mvn clean install -q )
(cd instrumentation ; mvn exec:java -q )
(cd web-server ; mvn exec:java -q)
#mvn clean install -q && mvn -pl instrumentation exec:java -q && (cd web-server ; mvn exec:java -q)
