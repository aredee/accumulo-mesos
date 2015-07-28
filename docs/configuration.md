# Configuration Nightmare

This is an attempt to document what gets configured where, and how that information gets to the process that needs it.


## Framework

Required Environment Variables:
ACCUMULO_HOME
HADOOP_PREFIX
HADOOP_CONF_DIR
ZOOKEEPER_HOME
ACCUMULO_CLIENT_CONF_PATH - location of accumulo-site.xml

Required Configuration:
bind address: ip address to bind web service to.
port: port to bind web service to.
zookeeper: location(s) of zookeeper nodes
mesos: location of mesos master
framework: a name for this framework
tarball: URI of accumulo-mesos-dist tarball

Launching
-Xmx
-Xms

## Scheduler








