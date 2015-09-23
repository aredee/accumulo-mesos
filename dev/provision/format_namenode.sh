#!/bin/bash -v

set -e

# Format NameNode
sudo -u hduser sh -c 'yes Y | /usr/local/hadoop/bin/hdfs namenode -format'
