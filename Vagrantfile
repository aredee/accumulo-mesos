# -*- mode: ruby -*-
# # vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

$provision_script = <<SCRIPT

PREFIX="PROVISIONER:"

set -e

echo "${PREFIX} Installing pre-reqs..."

# For installing Java 8
add-apt-repository ppa:webupd8team/java

# For Mesos
apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | sudo tee /etc/apt/sources.list.d/mesosphere.list

apt-get -y update
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
apt-get -y install oracle-java8-installer
apt-get -y install oracle-java8-set-default
apt-get -y install libcurl3
apt-get -y install zookeeperd
apt-get -y install aria2
apt-get -y install ssh
apt-get -y install rsync


MESOS_VERSION="0.21.1"
echo "${PREFIX}Installing mesos version: ${MESOS_VERSION}..."
apt-get -y install mesos

#Install docker
#sudo apt-get -y install linux-image-generic-lts-trusty
#curl -sSL https://get.docker.com/ | sh

echo "Done"

ln -s /usr/lib/jvm/java-8-oracle/jre/lib/amd64/server/libjvm.so /usr/lib/libjvm.so

echo "${PREFIX}Successfully provisioned machine for development"

SCRIPT

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  #config.vm.box = "ubuntu/trusty64"
  #config.vm.box_url = "trusty64.box"
  config.vm.box_url = "https://vagrantcloud.com/ubuntu/boxes/trusty64"

  config.vm.provision "shell", inline: $provision_script

  # Configure VM resources
  config.vm.provider :virtualbox do |vb|
    vb.customize ["modifyvm", :id, "--memory", "4096"]
    vb.customize ["modifyvm", :id, "--cpus", "2"]
  end

  config.vm.define "master" do |node1|
            node1.vm.box = "ubuntu/trusty64"
	    node1.vm.hostname = "master"
            node1.vm.network :private_network, ip: "192.168.50.101"
	    node1.vm.network "forwarded_port",  guest: 8080, host: 8080
	    node1.vm.network "forwarded_port",  guest: 8000, host: 8000
	    node1.vm.network "forwarded_port",  guest: 8081, host: 8081
	    node1.vm.network "forwarded_port",  guest: 5050, host: 5050
  	    node1.vm.network "forwarded_port",  guest: 5051, host: 5051
  	node1.vm.network "forwarded_port", guest: 50070, host: 50070
  	node1.vm.network "forwarded_port", guest: 50075, host: 50075
  	node1.vm.network "forwarded_port", guest: 50095, host: 50095
  	node1.vm.network "forwarded_port", guest: 8088, host: 8088
  	node1.vm.network "forwarded_port", guest: 8042, host: 8042
  	node1.vm.network "forwarded_port", guest: 19888, host: 19888
  	node1.vm.network "forwarded_port", guest: 8192, host: 8192
  	node1.vm.network "forwarded_port", guest: 2181, host: 2181

            node1.vm.provision "shell", path: "startmaster.sh", args: "192.168.50.101"
   end

   config.vm.define "slave" do |node2|
            node2.vm.box = "ubuntu/trusty64"
	    node2.vm.hostname = "slave"
            node2.vm.network :private_network, ip: "192.168.50.102"
	    node2.vm.network "forwarded_port",  guest: 8080, host: 80802
	    node2.vm.network "forwarded_port",  guest: 8000, host: 80002
	    node2.vm.network "forwarded_port",  guest: 8081, host: 80812
	    node2.vm.network "forwarded_port",  guest: 5050, host: 50502
  	    node2.vm.network "forwarded_port",  guest: 5051, host: 50512
  	    node2.vm.network "forwarded_port",  guest: 22, host: 50022
       node2.vm.network "forwarded_port", guest: 50070, host: 90070
        node2.vm.network "forwarded_port", guest: 50075, host: 90075
        node2.vm.network "forwarded_port", guest: 50095, host: 90095
        node2.vm.network "forwarded_port", guest: 8088, host: 8086
        node2.vm.network "forwarded_port", guest: 8042, host: 8046
        node2.vm.network "forwarded_port", guest: 19888, host: 19886

            node2.vm.provision "shell", path: "startslave.sh", args: "192.168.50.102"
   end
   config.vm.define "slave3" do |node3|
            node3.vm.box = "ubuntu/trusty64"
            node3.vm.hostname = "slave3"
            node3.vm.network :private_network, ip: "192.168.50.103"
            node3.vm.network "forwarded_port",  guest: 8080, host: 80803
            node3.vm.network "forwarded_port",  guest: 8000, host: 80003
            node3.vm.network "forwarded_port",  guest: 8081, host: 80813
            node3.vm.network "forwarded_port",  guest: 5050, host: 50503
            node3.vm.network "forwarded_port",  guest: 5051, host: 50513
  	    node3.vm.network "forwarded_port",  guest: 22, host: 50023
       node3.vm.network "forwarded_port", guest: 50070, host: 91070
        node3.vm.network "forwarded_port", guest: 50075, host: 91075
        node3.vm.network "forwarded_port", guest: 50095, host: 91095
        node3.vm.network "forwarded_port", guest: 8088, host: 8087
        node3.vm.network "forwarded_port", guest: 8042, host: 8047
        node3.vm.network "forwarded_port", guest: 19888, host: 19887


            node3.vm.provision "shell", path: "startslave.sh", args: "192.168.50.103"
   end


end
