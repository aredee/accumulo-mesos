# -*- mode: ruby -*-
# # vi: set ft=ruby :

VAGRANTFILE_API_VERSION = "2"

NUM_SLAVES = 6

# re-write /etc/hosts because ubuntu does 127.0.1.1 stuff that borks Hadoop
$host_script = <<SCRIPT
echo "127.0.0.1 localhost" > /etc/hosts
SCRIPT

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "klucar/jessie64_cgroup_mem"
    if Vagrant.has_plugin?("vagrant-cachier")
      # Configure cached packages to be shared between instances of the same base box.
      config.cache.scope = :box
    end


  #config.vm.box_url = "trusty64.box"
  #config.vm.box_url = "https://vagrantcloud.com/ubuntu/boxes/trusty64"
  config.hostmanager.enabled = false
  config.vm.provision "shell", path: "dev/provision/install_default_jre_headless.sh"
  config.vm.provision "shell", path: "dev/provision/install_mesos.sh"
  config.vm.provision "shell", path: "dev/provision/install_docker.sh"
  #config.vm.provision "shell", path: "dev/provision/install_compiler.sh"

  # Configure VM resources
  config.vm.provider :virtualbox do |vb|
    vb.customize ["modifyvm", :id, "--memory", "2048"]
    vb.customize ["modifyvm", :id, "--cpus", "2"]
    vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
    vb.customize ["modifyvm", :id, "--natdnsproxy1", "on"]
  end

  # master contains:
  #   Mesos master
  #   Zookeeper
  #   Namenode
  config.vm.define "master" do |node|
        node.vm.box = "klucar/jessie64_cgroup_mem"
        node.vm.hostname = "master"
        node.vm.network :private_network, ip: "172.16.0.100"
        node.vm.provider "virtualbox" do |v|
          v.memory = 2048
          v.cpus = 2
        end
        node.vm.network "forwarded_port", guest: 8080, host: 8080
        node.vm.network "forwarded_port", guest: 8000, host: 8000
        node.vm.network "forwarded_port", guest: 8081, host: 8081
        node.vm.network "forwarded_port", guest: 5050, host: 5050
        node.vm.network "forwarded_port", guest: 5051, host: 5051
        node.vm.network "forwarded_port", guest: 50070, host: 50070
        node.vm.network "forwarded_port", guest: 50075, host: 50075
        # accumulo monitor port
        node.vm.network "forwarded_port", guest: 50095, host: 50095
        node.vm.network "forwarded_port", guest: 8088, host: 8088
        node.vm.network "forwarded_port", guest: 8042, host: 8042
        node.vm.network "forwarded_port", guest: 19888, host: 19888
        node.vm.network "forwarded_port", guest: 8192, host: 8192
        node.vm.network "forwarded_port", guest: 2181, host: 2181

        node.vm.provision "shell", path: "dev/provision/start_mesos_master_debian.sh", args: "172.16.0.100"
        node.vm.provision "shell", path: "dev/provision/install_hadoop.sh", args: ["172.16.0.100","172.16.0.100"]
        node.vm.provision "shell", path: "dev/provision/format_namenode.sh"
        node.vm.provision "shell", path: "dev/provision/start_namenode.sh", run: "always"

  end

  # works up to 9 slaves because of ip address.
  (1..NUM_SLAVES).each do |i|
    config.vm.define "slave#{i}" do |node|
      node.vm.box = "klucar/jessie64_cgroup_mem"
      node.vm.hostname = "slave#{i}"

      # forward accumulo monitor port because we don't guarantee monitor and master live together.
      hostport_ = 50095 + i
      node.vm.network "forwarded_port", guest: 50095, host: hostport_

      node.vm.provider "virtualbox" do |v|
        v.memory = 2048
        v.cpus = 2
      end

      node.vm.network :private_network, ip: "172.16.0.10#{i}"
      node.vm.provision "shell", path: "dev/provision/install_hadoop.sh", args: ["172.16.0.100","172.16.0.10#{i}"]
      node.vm.provision "shell", path: "dev/provision/start_mesos_slave_debian.sh", args: ["172.16.0.10#{i}", "172.16.0.100", "slave#{i}"]
      node.vm.provision "shell", path: "dev/provision/start_datanode.sh", run: "always"

    end
  end

  config.vm.provision "shell", inline: $host_script
  config.vm.provision :hostmanager

end
