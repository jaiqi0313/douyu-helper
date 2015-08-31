#!/bin/bash
#project directory

if [ -z "$BASE_DIR" ] ; then
  PRG="$0"

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
      PRG="$link"
    else
      PRG="`dirname "$PRG"`/$link"
    fi
  done
  BASE_DIR=`dirname "$PRG"`/..

  # make it fully qualified
  BASE_DIR=`cd "$BASE_DIR" && pwd`
fi

#Config your java home
#JAVA_HOME=/opt/jdk/

if [ -z "$JAVA_HOME" ]; then
  export JAVA=`which java`
else
  export JAVA="$JAVA_HOME/bin/java"
fi

export CLASSPATH=$CLASSPATH:$BASE_DIR/conf:$(ls $BASE_DIR/lib/*.jar | tr '\n' :)

$JAVA -cp $CLASSPATH io.github.hengyunabc.douyuhelper.HelperMain
