#!/bin/bash



export ACCUMULO_HOME=/opt/accumulo/accumulo-1.7.0
export ACCUMULO_CLIENT_CONF_PATH=$ACCUMULO_HOME/conf
export HADOOP_PREFIX=/usr/local/hadoop
export HADOOP_CONF_DIR=$HADOOP_PREFIX/etc/hadoop
export ZOOKEEPER_HOME=/etc/zookeeper


HADOOP_HOME=/usr/local/hadoop
HADOOP_NAMENODE=172.31.45.229:54310
ACCUMULO_TAR="accumulo-1.7.0.tar.gz"
ACCUMULO_NATIVE_LIB="libaccumulo.so"
ACCUMULO_DIST="accumulo-mesos-dist-0.2.0-SNAPSHOT.tar.gz"

FOUND_ACCUMULO=`${HADOOP_HOME}/bin/hadoop fs -ls hdfs://${HADOOP_NAMENODE}/dist | grep ${ACCUMULO_TAR}`
FOUND_ACCUMULO_NATIVE_LIB=`${HADOOP_HOME}/bin/hadoop fs -ls hdfs://${HADOOP_NAMENODE}/dist | grep ${ACCUMULO_NATIVE_LIB}`
FOUND_ACCUMULO_DIST=`${HADOOP_HOME}/bin/hadoop fs -ls hdfs://${HADOOP_NAMENODE}/dist | grep ${ACCUMULO_DIST}`

# Fix broken packages
#apt-get -f install
apt-get clean
mv /var/lib/apt/lists /tmp
mkdir -p /var/lib/apt/lists/partial
apt-get clean
apt-get update

Read more: http://www.sillycodes.com/2015/06/quick-tip-couldnt-create-temporary-file.html#ixzz3pDRsLcw3 
Under Creative Commons License: Attribution 
Follow us: Mvenkatesh431 on Facebook
# Create accumulo-mesos folder
${HADOOP_HOME}/bin/hadoop fs -mkdir hdfs://${HADOOP_NAMENODE}/accumulo-mesos

# Ensure config files are present
if [ ! -f ${CLUSTER_CONFIG} ]; then
    echo "[FATAL] ${CLUSTER_CONFIG} not found. Did you mount your volumes?"
    exit 1
fi
if [ ! -f ${FRAMEWORK_CONFIG} ]; then
    echo "[FATAL] ${FRAMEWORK_CONFIG} not found. Did you mount your volumes?"
    exit 1
fi

# Ensure libraries are mounted
if [ ! -f /accumulo-lib/${ACCUMULO_DIST} ]; then
    echo "[FATAL] ${ACCUMULO_DIST} not found in /accumulo-lib. Did you mount your volumes?"
    ls -lath /accumulo-lib/
    exit 1
fi


# Ensure env vars are present
if [ -z $HADOOP_NAMENODE ]; then
    echo "[FATAL] HADOOP_NAMENODE env variable not set!"
    exit 1
fi


# Look for accumulo tar 
if [ "${FOUND_ACCUMULO}" > 0 ]; then
  echo "[FOUND] ${ACCUMULO_TAR}"
else
  echo "[MISSING] ${ACCUMULO_TAR}"
  mv /opt/accumulo.tgz /opt/${ACCUMULO_TAR}
  ${HADOOP_HOME}/bin/hadoop fs -copyFromLocal /opt/${ACCUMULO_TAR} hdfs://${HADOOP_NAMENODE}/dist/
  echo "[COPIED] ${ACCUMULO_TAR} to HDFS"
fi


# Look for accumulo native library in HDFS
if [ "${FOUND_ACCUMULO_NATIVE_LIB}" > 0 ];then
  echo "[FOUND] ${ACCUMULO_NATIVE_LIB}"
else
  echo "[MISSING] ${ACCUMULO_NATIVE_LIB} Compiling..."
  rm /etc/apt/sources.list.d/webupd8team-java-trusty.list && apt-get update
  apt-get install -y build-essential g++ gcc
  ls -lath /usr/lib/jvm/java-8-oracle/include
  gcc -I /usr/lib/jvm/java-8-oracle/include
  
  . ${ACCUMULO_HOME}/bin/build_native_library.sh
fi

# Look for dist library in HDFS
if [ "${FOUND_ACCUMULO_NATIVE_LIB}" > 0 ];then
  echo "[FOUND] ${ACCUMULO_NATIVE_LIB}"
else
  echo "[MISSING] ${ACCUMULO_NATIVE_LIB}"
  
fi




