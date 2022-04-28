FROM openjdk:11.0.3-jre-slim-stretch
VOLUME /config
ADD target/file-demo-0.0.1-SNAPSHOT.jar /file-demo.jar
EXPOSE 8099
RUN bash -c 'touch /file-demo.jar'
ENTRYPOINT ["java","-jar","/file-demo.jar"]