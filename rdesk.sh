#!/bin/sh
if [ ! $# -eq 1 ]; then
   echo "$0 <terminal_number>"
   exit 1
fi
let tnum=$1
mac=`utdesktop -l | awk '{if ($2=='$tnum') print $1 }'`
[ -z "$mac" ] && exit
utku=`utsession -p | grep $mac | awk '{print $3}'`
disp=`utsession -p | grep $mac | awk '{print $4}'`
isuser=`echo $utku | grep ^utku`
[ "$isuser" ] && su - $utku -c "x11vnc -display :$disp" || echo "user not found."
