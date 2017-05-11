#!/usr/bin/env bash
echo "Hell yeah it's a brave world!!"
(cd common ; mvn clean install -q )
(cd storageserver ; mvn clean install exec:java -q )
