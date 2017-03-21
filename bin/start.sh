#!/bin/sh

# Usage : start.sh

DIRNAME=`dirname "$0"`
PROGNAME=`basename "$0"`
GREP="grep"

# OS specific support (must be 'true' or 'false').
cygwin=false;
darwin=false;
linux=false;
solaris=false;
freebsd=false;
other=false
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;

    Darwin*)
        darwin=true
        ;;
    FreeBSD)
        freebsd=true
        ;;
    Linux)
        linux=true
        ;;
    SunOS*)
        solaris=true
        ;;
    *)
        other=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$JAVAC_JAR" ] &&
        JAVAC_JAR=`cygpath --unix "$JAVAC_JAR"`
fi

JMS_REST_HOME=`cd "$DIRNAME/.."; pwd`
export JMS_REST_HOME

# LOG file
if [ -z "$JMS_REST_LOG_DIR" ]; then
  JMS_REST_LOG_DIR=$JMS_REST_HOME/logs
fi

# Load JMS REST init.d configuration.
if [ -z "$JMS_REST_YAML" ]; then
  JMS_REST_YAML="$JMS_REST_HOME/conf/jms-rest.yaml"
fi

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
        JAVA="$JAVA_HOME/bin/java"
    else
        JAVA="java"
    fi
fi

if [ "$PRESERVE_JAVA_OPTS" != "true" ]; then
    # Check for -d32/-d64 in JAVA_OPTS
    JVM_D64_OPTION=`echo $JAVA_OPTS | $GREP "\-d64"`
    JVM_D32_OPTION=`echo $JAVA_OPTS | $GREP "\-d32"`

    # Check If server or client is specified
    SERVER_SET=`echo $JAVA_OPTS | $GREP "\-server"`
    CLIENT_SET=`echo $JAVA_OPTS | $GREP "\-client"`

    if [ "x$JVM_D32_OPTION" != "x" ]; then
        JVM_OPTVERSION="-d32"
    elif [ "x$JVM_D64_OPTION" != "x" ]; then
        JVM_OPTVERSION="-d64"
    elif $darwin && [ "x$SERVER_SET" = "x" ]; then
        # Use 32-bit on Mac, unless server has been specified or the user opts are incompatible
        "$JAVA" -d32 $JAVA_OPTS -version > /dev/null 2>&1 && PREPEND_JAVA_OPTS="-d32" && JVM_OPTVERSION="-d32"
    fi

    CLIENT_VM=false
    if [ "x$CLIENT_SET" != "x" ]; then
        CLIENT_VM=true
    elif [ "x$SERVER_SET" = "x" ]; then
        if $darwin && [ "$JVM_OPTVERSION" = "-d32" ]; then
            # Prefer client for Macs, since they are primarily used for development
            CLIENT_VM=true
            PREPEND_JAVA_OPTS="$PREPEND_JAVA_OPTS -client"
        else
            PREPEND_JAVA_OPTS="$PREPEND_JAVA_OPTS -server"
        fi
    fi

    if [ $CLIENT_VM = false ]; then
        NO_COMPRESSED_OOPS=`echo $JAVA_OPTS | $GREP "\-XX:\-UseCompressedOops"`
        if [ "x$NO_COMPRESSED_OOPS" = "x" ]; then
            "$JAVA" $JVM_OPTVERSION -server -XX:+UseCompressedOops -version >/dev/null 2>&1 && PREPEND_JAVA_OPTS="$PREPEND_JAVA_OPTS -XX:+UseCompressedOops"
        fi
    fi

    JAVA_OPTS="$PREPEND_JAVA_OPTS $JAVA_OPTS"
fi

if [ "$PRESERVE_JAVA_OPTS" != "true" ]; then
    # Check for -d32/-d64 in JAVA_OPTS
    JVM_D64_OPTION=`echo $JAVA_OPTS | $GREP "\-d64"`
    JVM_D32_OPTION=`echo $JAVA_OPTS | $GREP "\-d32"`

    # Check If server or client is specified
    SERVER_SET=`echo $JAVA_OPTS | $GREP "\-server"`
    CLIENT_SET=`echo $JAVA_OPTS | $GREP "\-client"`

    if [ "x$JVM_D32_OPTION" != "x" ]; then
        JVM_OPTVERSION="-d32"
    elif [ "x$JVM_D64_OPTION" != "x" ]; then
        JVM_OPTVERSION="-d64"
    elif $darwin && [ "x$SERVER_SET" = "x" ]; then
        # Use 32-bit on Mac, unless server has been specified or the user opts are incompatible
        "$JAVA" -d32 $JAVA_OPTS -version > /dev/null 2>&1 && PREPEND_JAVA_OPTS="-d32" && JVM_OPTVERSION="-d32"
    fi

    CLIENT_VM=false
    if [ "x$CLIENT_SET" != "x" ]; then
        CLIENT_VM=true
    elif [ "x$SERVER_SET" = "x" ]; then
        if $darwin && [ "$JVM_OPTVERSION" = "-d32" ]; then
            # Prefer client for Macs, since they are primarily used for development
            CLIENT_VM=true
            PREPEND_JAVA_OPTS="$PREPEND_JAVA_OPTS -client"
        else
            PREPEND_JAVA_OPTS="$PREPEND_JAVA_OPTS -server"
        fi
    fi

    JAVA_OPTS="$PREPEND_JAVA_OPTS $JAVA_OPTS"
fi

# Display our environment
echo "========================================================================="
echo ""
echo "  JMS Rest Bridge Environment"
echo ""
echo "  JMS_REST_YAML: $JMS_REST_YAML"
echo ""
echo "  JMS_REST_LOG_DIR: $JMS_REST_LOG_DIR"
echo ""
echo "  JAVA: $JAVA"
echo ""
echo "  JAVA_OPTS: $JAVA_OPTS"
echo ""
echo "========================================================================="
echo ""

while true; do
   if [ "x$LAUNCH_JMS_REST_IN_BACKGROUND" = "x" ]; then
      # Execute the JVM in the foreground
      eval \"$JAVA\" -D\"[Standalone]\" $JAVA_OPTS \
         \"-Dlog4j.configuration=file:$JMS_REST_HOME/conf/log4j.properties\" \
	 \"-Dlog.dir=$JMS_REST_LOG_DIR\" \
         -jar \"$JMS_REST_HOME/jms-rest-bridge.jar\" \
         "$JMS_REST_YAML"
      JMS_REST_STATUS=$?
   else
      # Execute the JVM in the background
      eval \"$JAVA\" -D\"[Standalone]\" $JAVA_OPTS \
         \"-Dlog4j.configuration=file:$JMS_REST_HOME/conf/log4j.properties\" \
	 \"-Dlog.dir=$JMS_REST_LOG_DIR\" \
         -jar \"$JMS_REST_HOME/jms-rest-bridge.jar\" \
         "$JMS_REST_YAML" "&"
      JMS_REST_PID=$!
      # Trap common signals and relay them to the bridge process
      trap "kill -HUP  $JMS_REST_PID" HUP
      trap "kill -TERM $JMS_REST_PID" INT
      trap "kill -QUIT $JMS_REST_PID" QUIT
      trap "kill -PIPE $JMS_REST_PID" PIPE
      trap "kill -TERM $JMS_REST_PID" TERM
      if [ "x$JMS_REST_PIDFILE" != "x" ]; then
        echo $JMS_REST_PID > $JMS_REST_PIDFILE
      fi
      # Wait until the background process exits
      WAIT_STATUS=128
      while [ "$WAIT_STATUS" -ge 128 ]; do
         wait $JMS_REST_PID 2>/dev/null
         WAIT_STATUS=$?
         if [ "$WAIT_STATUS" -gt 128 ]; then
            SIGNAL=`expr $WAIT_STATUS - 128`
            SIGNAL_NAME=`kill -l $SIGNAL`
            echo "*** JMS REST process ($JMS_REST_PID) received $SIGNAL_NAME signal ***" >&2
         fi
      done
      if [ "$WAIT_STATUS" -lt 127 ]; then
         JMS_REST_STATUS=$WAIT_STATUS
      else
         JMS_REST_STATUS=0
      fi
      if [ "$JMS_REST_STATUS" -ne 10 ]; then
            # Wait for a complete shudown
            wait $JMS_REST_PID 2>/dev/null
      fi
      if [ "x$JMS_REST_PIDFILE" != "x" ]; then
            grep "$JMS_REST_PID" $JMS_REST_PIDFILE && rm $JMS_REST_PIDFILE
      fi
   fi
   if [ "$JMS_REST_STATUS" -eq 10 ]; then
      echo "Restarting JMS-REST Bridge ..."
   else
      exit $JMS_REST_STATUS
   fi
done
