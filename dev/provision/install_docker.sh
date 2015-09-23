#!/bin/bash -v

set -e

#Install docker
apt-get update
apt-get -y install linux-image-generic-lts-trusty
curl -sSL https://get.docker.com/ | sh

