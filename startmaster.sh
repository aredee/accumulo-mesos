#!/bin/bash

sudo echo "Starting master!! $@" > startmaster.log

sudo echo "zk://$@:2181/mesos" > /etc/mesos/zk
sudo echo "$@ master" >> /etc/hosts
sudo echo "192.168.50.102 slave" >> /etc/hosts
sudo echo $@ > /etc/mesos-master/ip
sudo echo $@ > /etc/mesos-slave/ip

sudo start mesos-master 
sudo start mesos-slave
