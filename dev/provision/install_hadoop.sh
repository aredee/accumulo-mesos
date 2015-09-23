#!/bin/bash -v

# $1 is ip of namenode, $2 is ip of slave

set -e

HADOOP_VER=2.5.2

apt-get update

apt-get install -y openssh-server
apt-get install -y tar
apt-get install -y gzip

# Add hduser user and hadoop group

if [ `/bin/egrep  -i "^hadoop:" /etc/group` ]; then
   echo "Group hadoop already exists"
else
  echo "Adding hadoop group"
  addgroup hadoop
fi


if [ `/bin/egrep  -i "^hduser:" /etc/passwd` ]; then
  echo "User hduser already exists"
else
  echo "creating hduser in group hadoop"
  adduser --ingroup hadoop --disabled-password --gecos "" --home /home/hduser hduser
  adduser hduser sudo
fi


# Setup password-less auth
sudo -u hduser sh -c "mkdir /home/hduser/.ssh"
sudo -u hduser sh -c "chmod 700 /home/hduser/.ssh"
sudo -u hduser sh -c "yes | ssh-keygen -t rsa -N '' -f /home/hduser/.ssh/id_rsa"
sudo -u hduser sh -c 'cat /home/hduser/.ssh/id_rsa.pub >> /home/hduser/.ssh/authorized_keys'
sudo -u hduser sh -c "ssh-keyscan -H $1 >> /home/hduser/.ssh/known_hosts"
sudo -u hduser sh -c "ssh-keyscan -H localhost >> /home/hduser/.ssh/known_hosts"
sudo -u hduser sh -c "ssh-keyscan -H $2 >> /home/hduser/.ssh/known_hosts"

# Download Hadoop
# I've decided to provide this under the vagrant directory
#cd ~
#if [ ! -f hadoop-${HADOOP_VER}.tar.gz ]; then
#	wget http://apache.osuosl.org/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz
#fi

sudo tar ixzf /vagrant/dev/provision/tarballs/hadoop-${HADOOP_VER}.tar.gz -C /usr/local
cd /usr/local
rm -rf hadoop
sudo mv -f hadoop-${HADOOP_VER} hadoop
sudo chown -R hduser:hadoop hadoop

# Init bashrc with hadoop env variables
sudo sh -c 'echo export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_INSTALL=/usr/local/hadoop >> /home/hduser/.bashrc'
sudo sh -c 'echo export PATH=\$PATH:\$HADOOP_INSTALL/bin >> /home/hduser/.bashrc'
sudo sh -c 'echo export PATH=\$PATH:\$HADOOP_INSTALL/sbin >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_MAPRED_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_COMMON_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_HDFS_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sudo sh -c 'echo export YARN_HOME=\$HADOOP_INSTALL >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_COMMON_LIB_NATIVE_DIR=\$\{HADOOP_INSTALL\}/lib/native >> /home/hduser/.bashrc'
sudo sh -c 'echo export HADOOP_OPTS=\"-Djava.library.path=\$HADOOP_INSTALL/lib\" >> /home/hduser/.bashrc'
# hit the vagrant user with the same thing
sudo sh -c 'echo export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64 >> /home/vagrant/.bashrc'
sudo sh -c 'echo export HADOOP_INSTALL=/usr/local/hadoop >> /home/vagrant/.bashrc'
sudo sh -c 'echo export PATH=\$PATH:\$HADOOP_INSTALL/bin >> /home/vagrant/.bashrc'
sudo sh -c 'echo export PATH=\$PATH:\$HADOOP_INSTALL/sbin >> /home/vagrant/.bashrc'
sudo sh -c 'echo export HADOOP_MAPRED_HOME=\$HADOOP_INSTALL >> /home/vagrant/.bashrc'
sudo sh -c 'echo export HADOOP_COMMON_HOME=\$HADOOP_INSTALL >> /home/vagrant/.bashrc'
sudo sh -c 'echo export HADOOP_HDFS_HOME=\$HADOOP_INSTALL >> /home/vagrant/.bashrc'
sudo sh -c 'echo export YARN_HOME=\$HADOOP_INSTALL >> /home/vagrant/.bashrc'
sudo sh -c 'echo export HADOOP_COMMON_LIB_NATIVE_DIR=\$\{HADOOP_INSTALL\}/lib/native >> /home/vagrant/.bashrc'
sudo sh -c 'echo export HADOOP_OPTS=\"-Djava.library.path=\$HADOOP_INSTALL/lib\" >> /home/vagrant/.bashrc'


# Modify JAVA_HOME in hadoop-env
cd /usr/local/hadoop/etc/hadoop
sudo -u hduser sed -i.bak s=\${JAVA_HOME}=//usr/lib/jvm/java-7-openjdk-amd64/=g hadoop-env.sh
pwd

/usr/local/hadoop/bin/hadoop version

# Update configuration
#sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>fs\.default\.name\</name>\<value>hdfs://localhost:9000\</value>\</property>=g' core-site.xml
sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>fs\.default\.name\</name>\<value>hdfs://'"$1"':9000\</value>\</property>=g' core-site.xml
sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>yarn\.nodemanager\.aux-services</name>\<value>mapreduce_shuffle</value>\</property>\<property>\<name>yarn.nodemanager.aux-services.mapreduce.shuffle.class</name>\<value>org\.apache\.hadoop\.mapred\.ShuffleHandler</value>\</property>=g' yarn-site.xml

sudo -u hduser cp mapred-site.xml.template mapred-site.xml
sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>mapreduce\.framework\.name</name>\<value>yarn</value>\</property>=g' mapred-site.xml

cd ~
sudo -u hduser sh -c 'mkdir -p ~hduser/mydata/hdfs/namenode'
sudo -u hduser sh -c 'mkdir -p ~hduser/mydata/hdfs/datanode'
sudo chown -R hduser:hadoop ~hduser/mydata

cd /usr/local/hadoop/etc/hadoop
sudo -u hduser sed -i.bak 's=<configuration>=<configuration>\<property>\<name>dfs\.replication</name>\<value>1\</value>\</property>\<property>\<name>dfs\.namenode\.name\.dir</name>\<value>file:/home/hduser/mydata/hdfs/namenode</value>\</property>\<property>\<name>dfs\.datanode\.data\.dir</name>\<value>file:/home/hduser/mydata/hdfs/datanode</value>\</property>\<property>\<name>dfs\.namenode\.datanode\.registration\.ip-hostname-check</name>\<value>false</value>\</property>=g' hdfs-site.xml





