#!/bin/bash -v


PREFIX="JAVA Default JDK Provisioner:"
set -e

# For installing Java 8
apt-get -y update
apt-get -y install default-jre-headless

if $(test -e /usr/lib/libjvm.so); then
  rm /usr/lib/libjvm.so
fi

ln -s /usr/lib/jvm/default-java/jre/lib/amd64/server/libjvm.so /usr/lib/libjvm.so

