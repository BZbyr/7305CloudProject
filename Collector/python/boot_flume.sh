#!/bin/bash
# 启动flume 读取twitter数据
nohup flume-ng agent -f /opt/spark-twitter/7305CloudProject/Collector/TwitterToKafka.conf -Dflume.root.logger=DEBUG,console -n a1  >> flume.log 2>&1 &


