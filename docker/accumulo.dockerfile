# Pull base image.
FROM ubuntu

RUN apt-get update
RUN apt-get install -y software-properties-common gpgv
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
#RUN add-apt-repository ppa:openjdk-r/ppa
RUN apt-get update

RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer


RUN apt-get install -y wget lsb-release
RUN DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]') CODENAME=$(lsb_release -cs) && echo "deb http://repos.mesosphere.com/${DISTRO} ${CODENAME} main" | tee /etc/apt/sources.list.d/mesosphere.list
RUN apt-get -y update
RUN apt-get install -y zookeeper

ADD bin/install-accumulo.sh /
RUN chmod +x /install-accumulo.sh
RUN /install-accumulo.sh

ADD bin/init-fs.sh /
RUN chmod +x /init-fs.sh
RUN /init-fs.sh

ADD bin/start-framework.sh /
RUN chmod +x /start-framework.sh

CMD ["/start-framework.sh"]
