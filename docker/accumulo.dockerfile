# Pull base image.
FROM debian

RUN apt-get update
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF

RUN apt-get install -y openjdk-7-jre-headless wget lsb-release
RUN DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]') CODENAME=$(lsb_release -cs) && echo "deb http://repos.mesosphere.com/${DISTRO} ${CODENAME} main" | tee /etc/apt/sources.list.d/mesosphere.list
RUN apt-get -y update
RUN apt-get install -y zookeeper

ADD bin/install-accumulo.sh /
RUN chmod +x /install-accumulo.sh
RUN /install-accumulo.sh

ADD bin/start-framework.sh /
RUN chmod +x /start-framework.sh

CMD ["/start-framework.sh"]
