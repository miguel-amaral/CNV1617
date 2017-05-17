#!/usr/bin/env bash
echo "" > metrics_time.csv
for file in average_04_*
do
    number=$(echo ${file} | sed 's/average_04_//g' | sed 's/_.*$//g')
    cat ${file} | grep avg | sed 's/avg //g' > ${number}_time
    time=$(cat ${number}_time)
    metric=$(cat ${number}_metric)
    echo "${metric} , ${time}" > ${number}_final
    echo "${metric} , ${time}" >> metrics_time.csv
done
echo "" >> metrics_time.csv

