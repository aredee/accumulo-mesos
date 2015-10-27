#!/bin/bash
cd ..
mvn package

cd docker
if [ ! -f "lib" ]; then
  mkdir lib
fi
echo "Copying libraries..."
cp ../accumulo-mesos-framework/target/accumulo-mesos-framework-0.2.0-SNAPSHOT-jar-with-dependencies.jar lib/
cp ../accumulo-mesos-dist/target/accumulo-mesos-dist-0.2.0-SNAPSHOT.tar.gz lib/

