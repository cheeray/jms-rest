#!/bin/sh
#
# JMS RESTful Bridge control script
# author: cheeray
# chkconfig: - 80 20
# description: JMS-REST startup script
# processname: jms-rest
# pidfile: /var/run/jms-rest/jms-rest.pid
# config: /etc/default/jms-rest.yaml
#

# Source function library.
. /etc/init.d/functions

# Load Java configuration.
[ -r /etc/java/java.conf ] && . /etc/java/java.conf
export JAVA_HOME

# Load JMS-REST init.d configuration.
if [ -z "$JMS_REST_CONF" ]; then
	JMS_REST_CONF="/etc/default/jms-rest.conf"
fi

[ -r "$JMS_REST_CONF" ] && . "${JMS_REST_CONF}"

# Set defaults.

if [ -z "$JMS_REST_HOME" ]; then
        JMS_REST_HOME=/opt/jms-rest
fi

if [ -z "$JMS_REST_USER" ]; then
        JMS_REST_USER=jms-rest
fi

if [ -z "$JMS_REST_SCRIPT" ]; then
        JMS_REST_SCRIPT=$JMS_REST_HOME/bin/start.sh
fi

if [ -z "$JMS_REST_PIDFILE" ]; then
	JMS_REST_PIDFILE=/var/run/jms-rest/jms-rest.pid
fi
export JMS_REST_PIDFILE

if [ -z "$JMS_REST_YAML" ]; then
	JMS_REST_YAML="/etc/default/jms-rest.yaml"
fi

if [ -z "$JMS_REST_LOG_DIR" ]; then
        JMS_REST_LOG_DIR=/var/log/jms-rest
fi

if [ -z "$JMS_REST_LOG" ]; then
        JMS_REST_LOG=$JMS_REST_LOG_DIR/bridge.log
fi

if [ -z "$STARTUP_WAIT" ]; then
	STARTUP_WAIT=30
fi

if [ -z "$SHUTDOWN_WAIT" ]; then
	SHUTDOWN_WAIT=30
fi

prog='jms-rest'

start() {
	echo -n "Starting $prog: "
	if [ -f $JMS_REST_PIDFILE ]; then
		read ppid < $JMS_REST_PIDFILE
		if [ `ps --pid $ppid 2> /dev/null | grep -c $ppid 2> /dev/null` -eq '1' ]; then
			echo -n "$prog is already running"
			failure
	echo
		return 1
	else
		rm -f $JMS_REST_PIDFILE
	fi
	fi
	mkdir -p $(dirname $JMS_REST_LOG)
	cat /dev/null > $JMS_REST_LOG

	mkdir -p $(dirname $JMS_REST_PIDFILE)
	chown $JMS_REST_USER $(dirname $JMS_REST_PIDFILE) || true

	if [ ! -z "$JMS_REST_USER" ]; then
		if [ -r /etc/rc.d/init.d/functions ]; then
			daemon --user $JMS_REST_USER LAUNCH_JMS_REST_IN_BACKGROUND=1 JMS_REST_PIDFILE=$JMS_REST_PIDFILE $JMS_REST_SCRIPT $JMS_REST_YAML >> $JMS_REST_LOG 2>&1 &
		else
			su - $JMS_REST_USER -c "LAUNCH_JMS_REST_IN_BACKGROUND=1 JMS_REST_PIDFILE=$JMS_REST_PIDFILE $JMS_REST_SCRIPT $JMS_REST_YAML " >> $JMS_REST_LOG 2>&1 &
		fi
	fi

	count=0
	launched=false

	until [ $count -gt $STARTUP_WAIT ]
	do
		grep 'JBAS015874:' $JMS_REST_LOG > /dev/null
		if [ $? -eq 0 ] ; then
			launched=true
			break
		fi
		sleep 1
		let count=$count+1;
	done

	success
	echo
	return 0
}

stop() {
	echo -n $"Stopping $prog: "
	count=0;

	if [ -f $JMS_REST_PIDFILE ]; then
		read kpid < $JMS_REST_PIDFILE
		let kwait=$SHUTDOWN_WAIT

		# Try issuing SIGTERM
		kill -15 $kpid
		until [ `ps --pid $kpid 2> /dev/null | grep -c $kpid 2> /dev/null` -eq '0' ] || [ $count -gt $kwait ]
			do
			sleep 1
			let count=$count+1;
		done

		if [ $count -gt $kwait ]; then
			kill -9 $kpid
		fi
	fi
	rm -f $JMS_REST_PIDFILE
	success
	echo
}

status() {
	if [ -f $JMS_REST_PIDFILE ]; then
		read ppid < $JMS_REST_PIDFILE
		if [ `ps --pid $ppid 2> /dev/null | grep -c $ppid 2> /dev/null` -eq '1' ]; then
			echo "$prog is running (pid $ppid)"
			return 0
		else
			echo "$prog dead but pid file exists"
			return 1
		fi
	fi
	echo "$prog is not running"
	return 3
}

case "$1" in
	start)
		start
		;;
	stop)
		stop
		;;
	restart)
		$0 stop
		$0 start
		;;
	status)
		status
		;;
	*)
		## If no parameters are given, print which are avaiable.
		echo "Usage: $0 {start|stop|status|restart|reload}"
		exit 1
		;;
esac
