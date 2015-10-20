##
# YARN installer script for Apache Myriad Deployment
##
HADOOP_VER="2.7.1"
HADOOP_TARBALL_URL=http://apache.osuosl.org/hadoop/common/hadoop-${HADOOP_VER}/hadoop-${HADOOP_VER}.tar.gz
ACCUMULO_TARBALL_URL=http://download.nextag.com/apache/accumulo/1.7.0/accumulo-1.7.0-bin.tar.gz


echo "Installing Yarn...."
if [ ! -z "$1" ];then
  HADOOP_TARBALL_URL=$1
  echo "Deleting previous hadoop home"
  rm -rf ${HADOOP_HOME}
fi

# Download the tarball
wget -O /opt/hadoop.tgz ${HADOOP_TARBALL_URL}
HADOOP_BASENAME=`basename ${HADOOP_TARBALL_URL} .tar.gz`

# Put in env defaults if they are missing
export HADOOP_GROUP=${HADOOP_GROUP:='hadoop'}
export HADOOP_USER=${HADOOP_USER:='accumulo'}
export HADOOP_HOME=${HADOOP_HOME:='/usr/local/hadoop'}
export USER_UID=${USER_UID:='113'}
export GROUP_UID=${GROUP_GID:='112'}

# Add hduser user
groupadd $HADOOP_GROUP -g ${GROUP_UID}
useradd $HADOOP_USER -g $HADOOP_GROUP -u ${USER_UID} -s /bin/bash
#mkdir /home/${HADOOP_USER}
chown -R $HADOOP_USER:$HADOOP_GROUP /home/${HADOOP_USER}

# Extract Hadoop
tar vxzf /opt/hadoop.tgz -C /tmp
#mv /tmp/hadoop-${HADOOP_VER} ${HADOOP_HOME}
echo "Moving /tmp/hadoop-${HADOOP_BASENAME} to ${HADOOP_HOME}"
mv /tmp/${HADOOP_BASENAME} ${HADOOP_HOME}
ls -lath ${HADOOP_HOME}

mkdir /home/$HADOOP_USER
chown -R ${HADOOP_USER}:${HADOOP_GROUP} ${HADOOP_HOME}

# Init bashrc with hadoop env variables
sh -c 'echo export JAVA_HOME=/usr >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export PATH=\$PATH:\${HADOOP_HOME}/bin >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export PATH=\$PATH:\${HADOOP_HOME}/sbin >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_MAPRED_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_COMMON_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_HDFS_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export YARN_HOME=\${HADOOP_HOME} >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_COMMON_LIB_NATIVE_DIR=\$\{HADOOP_HOME\}/lib/native >> /home/${HADOOP_USER}/.bashrc'
sh -c 'echo export HADOOP_OPTS=\"-Djava.library.path=\${HADOOP_HOME}/lib\" >> /home/${HADOOP_USER}/.bashrc'

# Link Mesos Libraries
touch ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh
echo "export JAVA_HOME=/usr" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh
echo "export MESOS_NATIVE_JAVA_LIBRARY=/usr/local/lib/libmesos.so" >> ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh

# Ensure the hadoop-env is executable
chmod +x ${HADOOP_HOME}/etc/hadoop/hadoop-env.sh



# install accumulo
ACCUMULO_HOME=/opt/accumulo

echo "Installing accumulo..."
if [ ! -z "$1" ];then
  ACCUMULO_TARBALL_URL=$1
  echo "Deleting previous accumulo home"
  rm -rf ${ACCUMULO_HOME}
fi

wget -O /opt/accumulo.tgz ${ACCUMULO_TARBALL_URL}
# Extract Accumulo
tar vxzf /opt/accumulo.tgz -C /tmp
#mv /tmp/accumulo-${HADOOP_VER} ${ACCUMULO_HOME}
echo "Moving /tmp/hadoop-${ACCUMULO_BASENAME} to ${ACCUMULO_HOME}"
mv /tmp/${ACCUMULO_BASENAME} ${ACCUMULO_HOME}
ls -lath ${ACCUMULO_HOME}
chown -R ${HADOOP_USER}:${HADOOP_GROUP} ${ACCUMULO_HOME}
