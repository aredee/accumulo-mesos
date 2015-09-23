#!/bin/bash -v

set -e

# Start NameNode
sudo -u hduser sh -c '/usr/local/hadoop/sbin/hadoop-daemon.sh start namenode'

sudo -u hduser sh -c "/usr/local/hadoop/bin/hadoop fs -chmod 777 /"