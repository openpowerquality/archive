#!/bin/sh
### BEGIN INIT INFO
# Provides:          opqd
# Required-Start:    $local_fs $network $named $time $syslog
# Required-Stop:     $local_fs $network $named $time $syslog
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Description:       opq power quality daemon
### END INIT INFO

SCRIPT=/usr/local/bin/opqd
LOGFILE=/var/log/opqd.log

PINCTL=/usr/local/bin/pinctl

export OPQD_SETTINGS_FILE=/usr/local/opqd/settings.set
MSP_PORT=/dev/ttyAMA0
MSP_SPEED=115200

start() {
  echo 'Configuring serial port' >&2
  stty -F $MSP_PORT cs8 $MSP_SPEED ignbrk -brkint -imaxbel -opost -onlcr -isig -icanon -iexten -echo -echoe -echok -echoctl -echoke noflsh -ixon -crtscts

  echo 'Reseting flow control' >&2
  $PINCTL  export 18
  $PINCTL  out 18
  $PINCTL  high 18
  echo "Starting opqd with the data folder $OPQD_SETTINGS_FILE" >&2
  sleep 15s # Try to make sure the network is up before running program
  $SCRIPT > $LOGFILE &
  echo 'Service started' >&2
}

stop() {
  echo 'Stopping service…' >&2
  killall opqd
  echo 'Service stopped' >&2
}


case "$1" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  restart)
    stop
    start
    ;;
  *)
    echo "Usage: $0 {start|stop|restart|uninstall}"
esac
