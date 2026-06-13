#指定基础镜像，在其上进行定制
FROM java:8

#复制上下文目录下的policy-web/target/policy-web-1.0-SNAPSHOT.jar 到容器里
COPY policy-web/target/policy-web-1.0-SNAPSHOT.jar policy-web.jar

#运行参数,包括jmx连接监控
ENV JAVA_OPTS="\
-server \
-Xms1024m \
-Xmx1024m \
-Xss1024k \
-XX:SurvivorRatio=8 \
-XX:+UseConcMarkSweepGC"

#指定容器启动程序及参数
ENTRYPOINT java -jar ${JAVA_OPTS} policy-web.jar
