Accumulo Mesos Framework
=========================

------------

**DISCLAIMER**
_This is a very early version of Accumulo-on-Mesos framework. This
document, code behavior, and anything else may change without notice and/or break older installations._

------------

# Design

# Current Status

### Implemented
* Project setup modified from (mesos-cassandra)[https://github.com/mesosphere/cassandra-mesos)

### Near Term Tasks
* none

# Running the Framework

# Configuration


### Install Maven
The Accumulo Mesos Framework requires an install of Maven 3.2.x.

### Setup maven toolchain for protoc

1. Download version 2.5.0 of protobuf [here](https://code.google.com/p/protobuf/downloads/list)
2. Install
  1. Linux (make sure g++ compiler is installed)
    1. Run the following commands to build protobuf

         ```
         tar xzf protobuf-2.5.0.tar.gz
         cd protobuf-2.5.0
         ./configure
         make
         ```

3. Create `~/.m2/toolchains.xml` with the following contents, Update `PROTOBUF_HOME` to match the directory you ran make in

  ```
  <?xml version="1.0" encoding="UTF-8"?>
  <toolchains>
    <toolchain>
      <type>protobuf</type>
      <provides>
        <version>2.5.0</version>
      </provides>
      <configuration>
        <protocExecutable>$PROTOBUF_HOME/src/protoc</protocExecutable>
      </configuration>
    </toolchain>
  </toolchains>
  ```

#### Resources
* https://developers.google.com/protocol-buffers/docs/downloads
* https://code.google.com/p/protobuf/downloads/list
* http://www.confusedcoders.com/random/how-to-install-protocol-buffer-2-5-0-on-ubuntu-13-04


