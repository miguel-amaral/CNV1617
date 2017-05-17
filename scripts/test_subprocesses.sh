#!/usr/bin/env bash
{ sleep 5; echo "waking up after 5 seconds;" } &
{ sleep 1; echo "waking up after 1 second;" } &
while true
do
    if [ -s pid ] ; then
        for pid in `cat pid`
        do
            echo "Checking the $pid"
            kill -0 "$pid" 2>/dev/null || sed -i "/^$pid$/d" pid
        done
    else
        echo "All your process completed" ## Do what you want here... here all your pids are in finished stated
        break
    fi
done
