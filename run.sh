mvn clean install

############### BY RAFA ###############
######## PROCEED WITH CAUTION #########
#######################################

cd Instrumentation/src/
javac $(find . -name "*.java")
export CLASSPATH=$CLASSPATH:$(pwd):./
cd ../../web-server/


#######################################
######## -------------------- #########
#######################################

mvn exec:java
