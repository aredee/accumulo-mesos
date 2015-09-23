#!/bin/bash -v

PREFIX="Mesos Provisioner: "
set -e

echo "${PREFIX} Installing pre-reqs..."
# For Mesos
apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | sudo tee /etc/apt/sources.list.d/mesosphere.list
apt-get -y update

apt-get -y install libcurl3
apt-get -y install zookeeperd
apt-get -y install aria2
apt-get -y install ssh
apt-get -y install rsync


MESOS_VERSION="0.22.1"
echo "${PREFIX}Installing mesos version: ${MESOS_VERSION}..."
apt-get -y install mesos
