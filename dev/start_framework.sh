#!/bin/bash


export ACCUMULO_HOME=/vagrant/dev/dist/accumulo-1.7.0
export ACCUMULO_CLIENT_CONF_PATH=$ACCUMULO_HOME/conf
export HADOOP_PREFIX=/usr/local/hadoop
export HADOOP_CONF_DIR=$HADOOP_PREFIX/etc/hadoop
export ZOOKEEPER_HOME=/etc/zookeeper

java -jar /vagrant/dev/dist/accumulo-mesos-dist-0.2.0-SNAPSHOT/accumulo-mesos-framework-0.2.0-SNAPSHOT-jar-with-dependencies.jar \
    -f /vagrant/dev/config/FrameworkAndAccumulo.json \
    | tee $LOG

