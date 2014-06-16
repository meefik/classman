#!/bin/sh

HOME_DIR=/opt/de_shell
LOG_DIR=/var/log/de_shell
PID_FILE=/var/run/de_shell.pid
TOKENS_FILE=/tmp/tokens
PATH=$PATH:/opt/SUNWut/sbin:/opt/SUNWkio/bin

case "$1" in
start)
    [ -r $PID_FILE ] && PID=`cat $PID_FILE`
    [ -n "$PID" ] && PS_MATCH=`ps -eo pid | grep $PID`
    if [ -n "$PS_MATCH" ]; then
        echo "This program is already launched!"
        exit 1
    fi
    datestamp=`date "+%d.%m.%Y %H:%M:%S"`
    echo "[$datestamp] Starting de_shell daemon..." >> $LOG_DIR/server.log
    cd $HOME_DIR/server
    java -cp $HOME_DIR/server Server >> $LOG_DIR/server.log 2>&1 &
    echo $! > $PID_FILE
    touch $TOKENS_FILE
    chmod 640 $TOKENS_FILE
    ;;
stop)
    [ -r $PID_FILE ] && PID=`cat $PID_FILE`
    [ -n "$PID" ] && PS_MATCH=`ps -eo pid | grep $PID`
    if [ -n "$PS_MATCH" ]; then
       datestamp=`date "+%d.%m.%Y %H:%M:%S"`
       echo "[$datestamp] Stopping de_shell daemon..." >> $LOG_DIR/server.log
       kill -9 `cat $PID_FILE`
    fi
    ;;
server)
   computers=$2
   desktops=`utdesktop -l | grep ^0 | awk '{if ($2!="") print $1":"$2}'`
   sessions=`utsession -p | grep ^pseudo | awk '{print $1":"$3}'`
   new_tokens=""
   for i in $desktops
   do
      desk_num=`echo $i | awk -F':' '{print $2}'`
      desk_token=`echo $i | awk -F':' '{print "pseudo."$1}'`
      active_comp=`echo $computers | grep ":$desk_num:"`
      active_sess=`echo $sessions | tr ' ' '\n' | grep "$desk_token"`
      active_user=`echo $active_sess | awk -F':' '{print $2}' | grep ^utku`
      session_type=`utkioskoverride -r $desk_token | grep ^SESSION_TYPE | awk -F'=' '{print $2}'`
      #echo "$desk_token . $desk_num . $active_comp . $active_sess . $active_user . $session_type"
      if [ -n "$active_comp" ]; then
         # set kiosk mode
         [ "$session_type" != "kiosk" ] && utkioskoverride -r $desk_token -s kiosk 1>/dev/null 2>/dev/null
       	 # kill	session
       	 [ -n "$active_sess" ] && [ -z "$active_user" ] && new_tokens="$new_tokens $desk_token" #utsession -k -t $desk_token
      else
         # set regular mode
         [ "$session_type" != "regular" ] && utkioskoverride -r $desk_token -s regular 1>/dev/null 2>/dev/null
         # kill session
         [ -n "$active_sess" ] && [ -n "$active_user" ] && new_tokens="$new_tokens $desk_token" #utsession -k -t $desk_token
      fi
   done
   #old_tokens=`cat $TOKENS_FILE`
   #for i in $old_tokens
   #do
   #   #echo $i
   #   utsession -k -t $i
   #done
   echo -n $new_tokens > ${TOKENS_FILE}
   setsid $0 clean > /dev/null 2>&1 < /dev/null &
   #echo -n $new_tokens > ${TOKENS_FILE}
   ;;
clean)
   self=`ps -eo pid,ppid,args | grep "manager.sh clean" | grep -v grep | grep -v $$ | wc -l`
   [ $self -gt 0 ] && exit
   kill_tokens=`cat ${TOKENS_FILE}`
   for i in $kill_tokens
   do
      #echo $i
      utsession -k -t $i
   done
   #echo -n > $TOKENS_FILE
   ;;
client)
   mac=`echo $SUN_SUNRAY_TOKEN | awk -F'.' '{print $2}'`
   compid=`utdesktop -l | grep ^$mac | awk '{print $2}'`
   if [ -n "$compid" ]; then
      #echo $compid
      LOG_FILE=${LOG_DIR}/client-${compid}.log
      date "+%d.%m.%Y %H:%M:%S" > $LOG_FILE
      echo $SUN_SUNRAY_TOKEN >> $LOG_FILE
      chmod 664 $LOG_FILE
      lsof_db=`lsof $HOME/.mozilla/firefox/default/cookies.sqlite | grep -v COMMAND | awk '{print $2}'`
      [ -n "$lsof_db" ] && kill -9 $lsof_db
      python $HOME_DIR/setcookie.py $compid >> $LOG_FILE 2>&1
      cd /opt/de_shell/client
      java -cp /opt/de_shell/client Client $compid >> $LOG_FILE 2>&1 &
      #java -jar client.jar $compid
   fi
   #setsid /usr/bin/terminal > /dev/null 2>&1 < /dev/null &
   ;;
profile)
   case "$2" in
   "guest")
      sed -i 's/user_pref("network\.proxy\.type", .);/user_pref("network\.proxy\.type", 2);/g' ~/.mozilla/firefox/default/prefs.js
      sed -i 's/user_pref("browser\.startup\.homepage", ".*");/user_pref("browser\.startup\.homepage", "http:\/\/de\.ifmo\.ru");/g' ~/.mozilla/firefox/default/prefs.js
      setsid /usr/bin/firefox > /dev/null 2>&1 < /dev/null &
   ;;
   "olymp")
      sed -i 's|user_pref("network\.proxy\.type", .);|user_pref("network.proxy.type", 0);|g' ~/.mozilla/firefox/default/prefs.js
      sed -i 's|user_pref("browser\.startup\.homepage", ".*");|user_pref("browser.startup.homepage", "http://cyber-net.spb.ru");|g' ~/.mozilla/firefox/default/prefs.js
      mac=`echo $SUN_SUNRAY_TOKEN | awk -F'.' '{print $2}'`
      compid=`utdesktop -l | grep ^$mac | awk '{print $2}'`
      echo 'user_pref("general.useragent.override", "Mozilla/5.0 (X11; U; Linux x86_64; ru; rv:1.9.2.18; CompID:'$compid') Gecko/20110622 Linux/3.6 Firefox/3.6.18");' >> ~/.mozilla/firefox/default/prefs.js
      setsid /usr/bin/firefox > /dev/null 2>&1 < /dev/null &
   ;;
   *)
      setsid /usr/bin/firefox > /dev/null 2>&1 < /dev/null &
   ;;
   esac
   ;;
warn)
   case "$2" in
   "info") notify-send -u normal -t 30000 -i dialog-information "Внимание!" $'До окончания текущего сеанса\nосталось меньше <b>5</b> минут.' ;;
   "exit") notify-send -u critical -t 30000 -i dialog-information "Внимание!" $'До окончания текущего сеанса\nосталось меньше <b>1</b> минуты.' ;;
   esac
   ;;
message)
   if [ -n "$2" ]; then
      msg=`echo $* | cut -c9-`
      notify-send -u low -t 600000 -i dialog-information "Сообщение" "$msg"
   fi
   ;;
esac
