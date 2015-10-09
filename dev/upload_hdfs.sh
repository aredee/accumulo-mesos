#!/bin/bash

echo "Cleaning local files"
rm -rf /vagrant/dev/dist/accumulo-mesos-dist-0.2.0-SNAPSHOT
rm /vagrant/dev/dist/accumulo-mesos-dist-0.2.0-SNAPSHOT.tar.gz
echo "Copying archives from build, expanding"
cp /vagrant/accumulo-mesos-dist/target/accumulo-mesos-dist-0.2.0-SNAPSHOT.tar.gz /vagrant/dev/dist/.
tar xzf /vagrant/dev/dist/accumulo-mesos-dist-0.2.0-SNAPSHOT.tar.gz -C /vagrant/dev/dist

TEST=`hadoop fs -ls /dist` 2>&1
if [ -z "$TEST" ]; then
  echo "Creating dist directory"
  hadoop fs -mkdir /dist
else
  echo "/dist already exists... skipping"
fi

echo "Uploading files to HDFS"
hadoop fs -copyFromLocal -f /vagrant/dev/dist/*.gz /dist/.
hadoop fs -copyFromLocal -f /vagrant/dev/dist/libaccumulo.so /dist/.
