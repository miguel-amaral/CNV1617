#!/usr/bin/env bash
normal_width=2000
normal_height=${normal_width}
percentage=5
small_width=$((normal_width*$percentage/100))
small_height=$((normal_height*$percentage/100))

number_rows=$((normal_height / $small_height))
number_columns=$((normal_width / $small_width))



for c in $(eval echo "{1..${number_columns}}")
do
    for r in $(eval echo "{1..${number_rows}}")
    do
        endpoint = "http://load-balancer-cnk.tk/metrics?
        echo "${c} : ${r}"
    done
done