FROM anapsix/alpine-java:8u201b09_jdk_unlimited

WORKDIR /app
COPY . /app

RUN crontab -l | { cat; echo "1 8 * * * java -jar /app/BilibiliTask-1.0.9-all.jar >> /var/log/bilibili_task.log 2>&1"; } | crontab -
RUN touch /var/log/bilibili_task.log

RUN sh gradlew clean build
RUN cp /app/build/libs/*.jar /app/
RUN sh gradlew clean

CMD crond && tail -f /var/log/bilibili_task.log
