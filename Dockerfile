FROM maven:3.6.3-jdk-14

ADD . /usr/src/webcrawler
WORKDIR /usr/src/webcrawler
EXPOSE 4567
ENTRYPOINT ["mvn", "clean", "verify", "exec:java"]