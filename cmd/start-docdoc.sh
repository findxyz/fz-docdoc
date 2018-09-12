#!/bin/bash

#-Dcom.sun.management.jmxremote.port=7749 \
#-Dcom.sun.management.jmxremote.authenticate=false \
#-Dcom.sun.management.jmxremote.ssl=false \
#-Djava.rmi.server.hostname=192.168.1.111 \

java -jar \
-Xmx1344M \
-Xms1344M \
-XX:MaxMetaspaceSize=256M \
-XX:MetaspaceSize=256M \
-XX:+UseG1GC \
-XX:MaxGCPauseMillis=100 \
-XX:+ParallelRefProcEnabled \
-XX:ErrorFile=/root/gcLog/hs_err_pid%p.log \
-XX:HeapDumpPath=/root/gcLog \
-XX:+HeapDumpOnOutOfMemoryError \
-Dlogback.configurationFile=/root/docdoc/logback.xml \
-Dip.url.cache=true \
docdoc-1.0-fat.jar > /dev/null 2>&1 \
& echo $! > ./pid.file