#!/bin/bash
# 启动flume 读取twitter数据
nohup flume-ng agent -c /opt/spark-twitter/twitter-collect -f TwitterToKafka.conf -Dflume.root.logger=DEBUG,console -n a1 >> flume.log 2>&1 &


