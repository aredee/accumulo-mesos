#!/bin/bash -v


PREFIX="JAVA 8 Provisioner:"
set -e

# For installing Java 8
add-apt-repository ppa:webupd8team/java
apt-get -y update
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
# apt-get -y install default-jdk
apt-get -y install oracle-java8-installer
apt-get -y install oracle-java8-set-default

if $(test -e /usr/lib/libjvm.so); then
  rm /usr/lib/libjvm.so
fi
ln -s /usr/lib/jvm/java-8-oracle/jre/lib/amd64/server/libjvm.so /usr/lib/libjvm.so

