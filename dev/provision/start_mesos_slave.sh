#!/bin/bash -v

# args => $1 = ip of slave, $2 = ip of mesos master, $3 = hostname of slave

set -e

echo "Starting slave $@" > startslave.log
echo "$1" > /etc/mesos-slave/ip
echo "cgroups/cpu,cgroups/mem" > /etc/mesos-slave/isolation
#echo "docker,mesos" > /etc/mesos-slave/containerizers
echo "mesos" > /etc/mesos-slave/containerizers
echo "/usr/local/hadoop" > /etc/mesos-slave/hadoop_home

echo "export HADOOP_HOME=/usr/local/hadoop" >> /root/.bashrc
echo "export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64" >> /root/.bashrc

echo "zk://$2:2181/mesos" | sudo tee /etc/mesos/zk
echo $1 | sudo tee /etc/mesos-slave/hostname
echo "cpus:2;mem:2048" | sudo tee /etc/mesos-slave/resources
echo manual | sudo tee /etc/init/mesos-master.override
echo manual | sudo tee /etc/init/zookeeper.override


start mesos-slave

