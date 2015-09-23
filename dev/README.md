/dev contains scripts and config used to develop and test the framework.

The main Vagrant file reads scripts from the provision directory to provision
a cluster running mesos, zookeeper and hdfs.

The dist directory contains archives to be uploaded to hdfs (accumulo, executor, etc)

The scripts here are meant to be run from inside the vagrant machines (i.e. vagrant ssh master ... cd /vagrant/dev ... do whatever)

Install /etc/hosts/ of all machines running.
vagrant plugin install vagrant-hostmanager
vagrant hostmanager

