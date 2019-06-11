FROM openjdk:8-jre-alpine
MAINTAINER Thiago Oliveira <pintowar@gmail.com>

RUN mkdir -p /opt/pbf
WORKDIR /opt/pbf
RUN wget http://download.geofabrik.de/europe/belgium-latest.osm.pbf

RUN mkdir -p /tmp/gh-tmp

WORKDIR /opt
ENV GRAPH_OSM_PATH /opt/pbf/belgium-latest.osm.pbf

EXPOSE 8080
ADD app.jar /opt/app.jar
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Djava.security.egd=file:/dev/./urandom -jar /opt/app.jar" ]
