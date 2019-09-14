FROM voidlinux/voidlinux
RUN xbps-install -Syu wget
RUN wget https://download.java.net/java/GA/jdk12/GPL/openjdk-12_linux-x64_bin.tar.gz -O /tmp/jdk.tar.gz &&  mkdir -p /opt/jvm &&  tar xfvz /tmp/jdk.tar.gz --directory /opt/jvm &&  rm -f /tmp/openjdk-11+28_linux-x64_bin.tar.gz
ENV PATH="$PATH:/opt/jvm/jdk-12/bin"
WORKDIR /root/
COPY build/libs/push-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/root/app.jar"]