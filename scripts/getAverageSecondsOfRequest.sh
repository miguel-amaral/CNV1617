#!/usr/bin/env bash
repetitions=10
file="04"
number=$1
scene_width=${number}
scene_height=${number}
small_width=${number}
small_height=${number}
collum_offset=0
rows_offset=0
endpoint="http://load-balancer-cnv.tk:8000/time?f=test${file}.txt&sc=${scene_width}&sr=${scene_height}&wc=${small_width}&wr=${small_height}&coff=${collum_offset}&roff=${rows_offset}"
doWebRequests=1
if [ ${doWebRequests} -ne 0 ]
then
    for try in $(eval echo "{1..${repetitions}}")
    do
        echo ${try}
        { wget ${endpoint} -O "results/average/${number}_${try}" -o /dev/null ; }
    done
fi
echo "REQUESTS ALL DONE"
echo
echo
echo
sum=0
min=100000000
max=0
for try in $(eval echo "{1..${repetitions}}")
do
    average_file="results/average/${number}_${try}"
#    echo "results/average/${try}"
    line=$(cat ${average_file})
    echo "result: $line"
    if(( $(echo "$line < $min" |bc -l) ))
    then
        min=${line}
    fi
    if(( $(echo "$line > max" |bc -l) ))
    then
        max=${line}
    fi
    sum=$(echo ${sum} + ${line} | bc -l)
done
echo
echo
echo
echo "repetitions ${repetitions}"
echo "sum ${sum}"
echo "avg $(echo ${sum} / ${repetitions} | bc -l)"
echo "min ${min}"
echo "max ${max}"
