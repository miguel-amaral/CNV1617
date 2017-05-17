#!/usr/bin/env bash
for number in 100 600 700 800 900 1000
do
    echo "Doing ${number}"
    ./getAverageSecondsOfRequest.sh ${number} > average_04_${number}_${number}
    ./getAverageMetrics.sh ${number}
#    cat average_04_${number}_${number}
#    read
done