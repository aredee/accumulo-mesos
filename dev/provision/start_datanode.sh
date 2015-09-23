#!/bin/bash -v

set -e

# Start DataNode
sudo -u hduser sh -c '/usr/local/hadoop/sbin/hadoop-daemons.sh start datanode'


