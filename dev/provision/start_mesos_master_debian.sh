#!/bin/bash -v

# $1 is the master ip

echo "Starting master!! $1" > startmaster.log

echo "zk://$1:2181/mesos" > /etc/mesos/zk
#echo "$@ master" >> /etc/hosts
#echo "192.168.50.102 slave" >> /etc/hosts
echo $1 | sudo tee /etc/mesos-master/ip
echo $1 | sudo tee /etc/mesos-master/hostname

echo "export HADOOP_HOME=/usr/local/hadoop" >> /root/.bashrc
echo "export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64" >> /root/.bashrc
echo "export PATH=$PATH:$HADOOP_HOME/bin:$JAVA_HOME/bin" >> /root/.bashrc

# keep mesos slave from starting here
echo manual | sudo tee /etc/init/mesos-slave.override

sudo service mesos-master start
