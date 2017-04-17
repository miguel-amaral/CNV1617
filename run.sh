cd $(mktemp -d) #go to new temporary dir
git clone git@github.com:miguel-amaral/CNV1617.git
cd CNV1617/
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
