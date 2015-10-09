Accumulo Mesos Framework
=========================
Initialize and run Accumulo clusters as a Mesos framework.

------------

**DISCLAIMER**
_This is a very early version of Accumulo-on-Mesos framework. This
document, code behavior, and anything else may change without notice and/or break older installations._

------------

# Design
The accumulo-mesos framework launches accumulo server processes on mesos client machines using
the `$ACCUMULO_HOME/bin/accumulo <server>` script. It automatically configures Java and Accumulo
memory settings based on the mesos offer. Initially the framework ran using the standard mesos
container, but due to a multitude of configuration differnces between clusters, it now runs
the processes inside a docker container.

# Current Status

### Implemented
* Framework no longer depends on accumulo! Accumulo is uploaded to HDFS. There are assumptions about
where somethings will be within the accumulo tarball when extracted, but this has been stable.

### Near Term Tasks
* Finalize docker implementation
* Have accumulo initialization as a separate step to running the framework so we don't kill
accumulo instances by mistake.
* Run accumulo init from a mesos client just like the accumulo servers.
* Flesh out the framework webservice.

# Running the Framework


## Configuration
See config examples in `dev/config`

## Testing
A multi-vm Vagrantfile is provided along with many provisioning scripts to setup
the VMs for testing the framework. See `/dev` directory for more info.

# Thanks
Thanks to the cassandra-mesos project. I stole a lot of the project setup and framework design ideas from there.
