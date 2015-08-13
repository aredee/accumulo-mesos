#!/bin/bash

sudo echo "Starting slave $@" > startslave.log
sudo echo "$@" > /etc/mesos-slave/ip
sudo echo "cgroups/cpu,cgroups/mem" > /etc/mesos-slave/isolation
sudo echo "docker,mesos" > /etc/mesos-slave/containerizers
sudo echo "$@ slave" >> /etc/hosts
sudo echo "192.168.50.101 master" >> /etc/hosts
sudo echo "zk://192.168.50.101:2181/mesos" > /etc/mesos/zk

# In case we are pulling a distro that is built locally
sudo mkdir -p /usr/local/libexec/mesos

sudo start mesos-slave

# a little surprised that the master gets started up
sudo stop mesos-master


