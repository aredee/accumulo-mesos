Accumulo Mesos Framework
=========================
Initialize and run Accumulo clusters as a Mesos framework.

------------

**DISCLAIMER**
_This is a very early version of accumulo-mesos framework. This
document, code behavior, and anything else may change without notice and/or break older installations._

------------

# Design
The accumulo-mesos framework launches accumulo server processes on mesos client machines using
the `$ACCUMULO_HOME/bin/accumulo <server>` script. It automatically configures Java and Accumulo
memory settings based on the mesos offer. The framework doesn't depend on Accumulo directly so
it should be able to support the more recent versions of Accumulo. It is tested with 1.7.0

# Current Status

### Implemented
* Framework no longer depends on accumulo! Accumulo is uploaded to HDFS. There are assumptions about
where somethings will be within the accumulo tarball when extracted, but this has been stable.
* Accumulo init is separate step from running the framework. Currently this requires a local copy of
Accumulo somewhere.

### Near Term Tasks
* Docker?
* Run accumulo init from a mesos client just like the accumulo servers.
* Flesh out the framework webservice.
* Reconnect framework to a running cluster.

# Running the Framework
First you have to upload artifacts to HDFS (accumulo tarball, framework tarball, native libs .so)
and copy that into your config JSON structures. (see `dev/config`)

Running the framework is then a two step process. First you must initialize the framework.
See `dev/init_framework.sh`  Then you can run the framework see `dev\start_framework.sh`


```
java -jar /vagrant/dev/dist/accumulo-mesos-dist-0.2.0-SNAPSHOT/accumulo-mesos-framework-0.2.0-SNAPSHOT-jar-with-dependencies.jar -h
usage: accumulo-mesos [-b <arg>] [-cc <arg>] [-fc <arg>] [-h] [-i] [-m
       <arg>] [-n <arg>] [-P <arg>] [-t <arg>] [-v] [-z <arg>]
 -b,--bind-address <arg>   IP address of interface to bind HTTP interface
                           to
 -cc,--cluster <arg>       JSON file containing cluster configuration
 -fc,--framework <arg>     JSON file of entire framework configuration
 -h,--help                 Print this message and exit
 -i,--init                 If present, initialize new Accumulo instance
 -m,--master <arg>         Location of mesos master to connect to
 -n,--name <arg>           Name of this mesos framework
 -P,--port <arg>           Port number to serve HTTP interface
 -t,--tarball <arg>        URI of framework tarball
 -v,--version              Show version number
 -z,--zookeepers <arg>     List of Zookeeper servers
 ```

## Configuration
See config examples in `dev/config`

## Testing
A multi-vm Vagrantfile is provided along with many provisioning scripts to setup
the VMs for testing the framework. See `/dev` directory for more info.

# Thanks
Thanks to the cassandra-mesos project. I stole a lot of the project setup and framework design ideas from there.
