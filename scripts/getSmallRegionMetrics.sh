#!/usr/bin/env bash
scene_width=2000
scene_height=${scene_width}
percentage=5
small_width=$((scene_width*percentage/100))
small_height=$((scene_height*percentage/100))

number_rows=$((scene_height / $small_height -1))
number_columns=$((scene_width / $small_width -1))


#number_columns=0
#number_rows=0
counter=0
subprocess=0
limit_processes=80
for file in 01 02 03 04 05
do
    for c in $(eval echo "{0..${number_columns}}")
    do
        collum_offset=$((c*small_width))
        for r in $(eval echo "{0..${number_rows}}")
        do
            rows_offset=$((r*small_height))
            if [ ${store_result} -ne 0 ]
            then
                save_file="results/${file}_${scene_width}_${scene_height}_${small_width}_${small_height}_${collum_offset}_${rows_offset}"
                letter="metrics"
            else
                save_file="/dev/null"
                letter="r.html"
            fi
            endpoint="http://load-balancer-cnv.tk:8000/${letter}?f=test${file}.txt&sc=${scene_width}&sr=${scene_height}&wc=${small_width}&wr=${small_height}&coff=${collum_offset}&roff=${rows_offset}"
            { wget ${endpoint} -O "$save_file" -o /dev/null  } &
            usleep 100
            echo "${c} : ${r} : ${endpoint}"
            counter=$((counter +1))
            subprocess=$((subprocess +1))
            if [ $subprocess -ge $limit_processes ]
            then
                wait
                subprocess=0
                echo "all done"
            fi
        done
    done
done
echo "$counter requests issued"
