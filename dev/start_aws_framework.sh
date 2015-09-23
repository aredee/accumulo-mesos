export ACCUMULO_HOME=/home/ubuntu/klucar/accumulo-1.7.0
export ACCUMULO_CLIENT_CONF_PATH=$ACCUMULO_HOME/conf
export HADOOP_PREFIX=/usr/lib/hadoop
export HADOOP_CONF_DIR=/etc/hadoop
export ZOOKEEPER_HOME=/etc/zookeeper

java -jar /home/ubuntu/klucar/accumulo-mesos-dist-0.2.0-SNAPSHOT/accumulo-mesos-framework-0.2.0-SNAPSHOT-jar-with-dependencies.jar \
    -f /home/ubuntu/klucar/AWS_framework.json \
    | tee $LOG

