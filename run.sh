mvn clean install

############### BY RAFA ###############
######## PROCEED WITH CAUTION #########
#######################################

export PROJ_DIR=$(pwd)
#export _JAVA_OPTIONS="-XX:-UseSplitVerifier "$_JAVA_OPTIONS

cd Instrumentation/src/
javac $(find . -name "*.java")

export CLASSPATH=$CLASSPATH:$(pwd):$(pwd)/tool:./
#java InstTool $PROJ_DIR/web-server/target/classes/raytracer $PROJ_DIR/web-server/target/classes/raytracer
cd ../../web-server/


#######################################
######## -------------------- #########
#######################################

mvn exec:java
