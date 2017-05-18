#!/usr/bin/env bash
pwd=$(pwd)
cd /tmp/one_at_time
sum=0
min=100000000
max=0
repetitions=$( ls -l | wc -l)
for file in *
do
    line=$(cat ${file} | sed 's:</br>:\n:g' | sed 's:</b><pre>::g' | sed 's:</pre><b>:\n:g' | grep Elapsed\ time | sed 's?Elapsed time: ??g')
    echo $line
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

echo "repetitions ${repetitions}"
echo "sum ${sum}"
echo "avg $(echo ${sum} / ${repetitions} | bc -l)"
echo "min ${min}"
echo "max ${max}"

cd ${pwd}