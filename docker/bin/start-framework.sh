#!/bin/bash


export ACCUMULO_HOME=/opt/accumulo/accumulo-1.7.0
export ACCUMULO_CLIENT_CONF_PATH=$ACCUMULO_HOME/conf
export HADOOP_PREFIX=/usr/local/hadoop
export HADOOP_CONF_DIR=$HADOOP_PREFIX/etc/hadoop
export ZOOKEEPER_HOME=/etc/zookeeper

export JAVA_HOME=/usr
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

# Look for accumulo0dst
if [ "${FOUND_ACCUMULO_DIST}" > 0 ]; then
  echo "[FOUND] ${ACCUMULO_DIST}"
else
  echo "[MISSING] ${ACCUMULO_DIST}"
  mv /opt/accumulo.tgz /opt/${ACCUMULO_DIST}
  ${HADOOP_HOME}/bin/hadoop fs -copyFromLocal /accumulo-lib/${ACCUMULO_DIST} hdfs://${HADOOP_NAMENODE}/dist/
  echo "[COPIED] ${ACCUMULO_DIST} to HDFS"
fi


# Look for accumulo native library in HDFS
if [ "${FOUND_ACCUMULO_NATIVE_LIB}" > 0 ];then
  echo "[FOUND] ${ACCUMULO_NATIVE_LIB}"
else
  echo "[MISSING] ${ACCUMULO_NATIVE_LIB} Compiling..."
  apt-get clean
  mv /var/lib/apt/lists /tmp
  mkdir -p /var/lib/apt/lists/partial
  apt-get clean
  apt-get update
  rm /etc/apt/sources.list.d/webupd8team-java-trusty.list && apt-get update
  apt-get install -y build-essential g++ gcc
  ls -lath /usr/lib/jvm/java-8-oracle/include
  gcc -I /usr/lib/jvm/java-8-oracle/include
  . ${ACCUMULO_HOME}/bin/build_native_library.sh
  mv ${ACCUMULO_HOME}/lib/native/libaccumulo.so /accumulo-lib/
  ${HADOOP_HOME}/bin/hadoop fs -copyFromLocal /accumulo-lib/${ACCUMULO_NATIVE_LIB} hdfs://${HADOOP_NAMENODE}/dist/
  echo "[COPIED] ${ACCUMULO_NATIVE_LIB} to HDFS"

fi

# Look for dist library in HDFS
if [ "${FOUND_ACCUMULO_NATIVE_LIB}" > 0 ];then
  echo "[FOUND] ${ACCUMULO_NATIVE_LIB}"
else
  echo "[MISSING] ${ACCUMULO_NATIVE_LIB}"
  
fi


${HADOOP_HOME}/bin/hadoop fs -rm -r hdfs://${HADOOP_NAMENODE}/accumulo-mesos/*

#init accumulo
java -jar /accumulo-lib/accumulo-mesos-framework-0.2.0-SNAPSHOT-jar-with-dependencies.jar \
    -i -fc /accumulo-config/framework.json -cc /accumulo-config/cluster.json \
    | tee $LOG



MESOS_MASTER="172.31.45.229:5050"
ZOOKEEPERS="172.31.20.165:2181"

java -jar /accumulo-lib/accumulo-mesos-framework-0.2.0-SNAPSHOT-jar-with-dependencies.jar \
     -master $MESOS_MASTER \
     -zookeepers $ZOOKEEPERS \
     -name accumulo-mesos-test-4 \
    | tee $LOG
