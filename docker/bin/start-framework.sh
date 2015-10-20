#!/bin/bash



export ACCUMULO_HOME=/opt/accumulo/accumulo-1.7.0
export ACCUMULO_CLIENT_CONF_PATH=$ACCUMULO_HOME/conf
export HADOOP_PREFIX=/usr/local/hadoop
export HADOOP_CONF_DIR=$HADOOP_PREFIX/etc/hadoop
export ZOOKEEPER_HOME=/etc/zookeeper


# Ensure myriad conf file is present
if [ ! -f $MYRIAD_CONFIG_FILE ]; then
    echo "[FATAL] Myriad config not found! - ${MYRIAD_CONFIG_FILE}"
    exit 1
fi
if [ ! -f $YARN_SITE ]; then
    echo "[FATAL] yarn-site.xml config not found! - ${YARN_SITE}"
    exit 1
fi

if [ -z $HADOOP_NAMENODE ]; then
    echo "[FATAL] HADOOP_NAMENODE env variable not set!"
    exit 1
fi
