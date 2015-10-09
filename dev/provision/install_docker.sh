#!/bin/bash -v

set -e

#Install docker
echo "deb http://http.debian.net/debian jessie-backports main" >> /etc/apt/sources.list
apt-get update
apt-get -y install docker.io
