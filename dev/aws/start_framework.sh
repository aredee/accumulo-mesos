#!/bin/bash

export LOG=/tmp/accumulo-framework.log

export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64
export ACCUMULO_HOME=/home/ubuntu/klucar/accumulo-1.7.0
export ACCUMULO_CLIENT_CONF_PATH=$ACCUMULO_HOME/conf
export HADOOP_PREFIX=/usr/lib/hadoop
export HADOOP_CONF_DIR=/etc/hadoop
export ZOOKEEPER_HOME=/etc/zookeeper

java -jar /home/ubuntu/klucar/accumulo-mesos-dist-0.2.0-SNAPSHOT/accumulo-mesos-framework-0.2.0-SNAPSHOT-jar-with-dependencies.jar \
     -master 172.31.1.11:5050 \
     -zookeepers 172.31.0.11:2181 \
     -name $1 \
    | tee $LOG


#    "bindAddress": "172.16.0.100",
#    "httpPort": "8192",
#    "mesosMaster": "172.16.0.100:5050",
#    "name":"accumulo-mesos-test",
#    "id": "",
#    "tarballUri": "hdfs://172.16.0.100:9000/dist/accumulo-mesos-dist-0.2.0-SNAPSHOT.tar.gz",
#    "zkServers": "172.16.0.100:2181"
