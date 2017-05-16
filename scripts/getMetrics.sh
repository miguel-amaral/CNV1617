#!/usr/bin/env bash
file="04"
scene_width=2000
scene_height=${scene_width}
percentage=5
small_width=$((scene_width*$percentage/100))
small_height=$((scene_height*$percentage/100))

number_rows=$((scene_height / $small_height))
number_columns=$((scene_width / $small_width))



for c in $(eval echo "{1..${number_columns}}")
do
    for r in $(eval echo "{1..${number_rows}}")
    do
        endpoint = "http://load-balancer-cnk.tk/metrics?f=test${file}.txt&sc=200&sr=200&wc=200&wr=200&coff=0&roff=0"
        echo "${c} : ${r}"
    done
done