#!/bin/sh
### BEGIN INIT INFO
# Provides:          configd
# Required-Start:    $local_fs $network $named $time $syslog
# Required-Stop:     $local_fs $network $named $time $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Description:       opq power quality config monitoring daemon
### END INIT INFO

CONFIG_PATH="/home/pi/Gen1/Software/rpi-firmware/rpid/config"
CONFIG_LOG="$CONFIG_PATH/config.log"
SCRIPT="$CONFIG_PATH/config.py"
CMD="sudo python $SCRIPT &"
PID="$CONFIG_PATH/config.pid"

start(){
  echo "Starting configd..." >> $CONFIG_LOG 2>&1
  echo "$CMD" >> $CONFIG_LOG 2>&1
  $CMD >> $CONFIG_LOG 2>&1
  echo "Setup process should be finished..." >> $CONFIG_LOG 2>&1
}

stop() {
  echo "Checking to see if process is still running..." >> $CONFIG_LOG 2>&1
  if [ -e $PID ]
  then
    echo "Process with PID=$PID found. Terminating process." >> $CONFIG_LOG 2>&1
    pid=`cat $PID`
    kill -9 $PID >> $CONFIG_LOG 2>&1
    echo "Process terminated." >> $CONFIG_LOG 2>&1
  else
    echo "PID for config.py not found. The process is most likely already dead." >> $CONFIG_LOG 2>&1
  fi
}

restart() {
  stop
  start
}

case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    restart
    ;;
  *)
    echo "Usage: /etc/init.d/configd {start|restart|stop}"
    exit 1
    ;;
esac

exit 0


