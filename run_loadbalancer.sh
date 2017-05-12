#!/usr/bin/env bash
echo "Its a brave new world!"
echo ""
echo "Compiling stuff"
(cd common ; mvn clean install -q )
(cd loadbalancer ; mvn clean install exec:java -q )
