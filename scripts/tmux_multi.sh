#!/usr/bin/env bash
session_name=$1
session_name="oals_2"
users=16
number=200
tmux kill-session -t ${session_name}
endpoint="load-balancer-cnv.tk:8000/r.html?f=test04.txt&sc=${number}&sr=${number}&wc=${number}&wr=${number}&coff=0&roff=0"
script="while true ; do wget \"$endpoint\" -o /dev/null -O /dev/null ;echo \"tick\"; done"
echo "${script}"
echo "${session_name}"
tmux new-session -d -s ${session_name} "${script}"
tmux select-window -t ${session_name}:0
tmux split-window -h "${script}"
tmux split-window -v -t 0 "${script}"
tmux split-window -v -t 1 "${script}"
#4 achieved
if [ ${users} -gt 4 ]
then
tmux split-window -h -t 0 "${script}"
tmux split-window -h -t 1 "${script}"
tmux split-window -h -t 2 "${script}"
tmux split-window -h -t 3 "${script}"
fi
#8 achieved

if [ ${users} -gt 8 ]
then
tmux split-window -v -t 0 "${script}"
tmux split-window -v -t 1 "${script}"
tmux split-window -v -t 2 "${script}"
tmux split-window -v -t 3 "${script}"
fi
#12

if [ ${users} -gt 12 ]
then
tmux split-window -v -t 4 "${script}"
tmux split-window -v -t 5 "${script}"
tmux split-window -v -t 6 "${script}"
tmux split-window -v -t 7 "${script}"
fi
if [ ${users} -gt 16 ]
then
    echo "not supported yet"
fi
#16
tmux -2 attach-session -t ${session_name}



