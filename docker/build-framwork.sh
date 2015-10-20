#!/bin/bash
cd ..
mvn package

cd docker
mkdir lib
cp ../accumulo-mesos-framework/target/accumulo-mesos-framework-0.2.0-SNAPSHOT-jar-with-dependencies.jar libs/


