#!/bin/bash

if [ -f pid ]
then
    pid=`cat pid`
    kill -9 $pid
    echo $pid 'is killed.'
    rm -f pid
    if [ $? == 0 ]; then echo 'pid is deleted.';fi
else
    echo 'no search pid file'
fi
