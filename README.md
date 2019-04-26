# CloudProject

Term Project for **COMP7305 Cluster and Cloud Computing**.

### Environment

[![](https://img.shields.io/badge/Hadoop-v2.7.5-blue.svg)](https://hadoop.apache.org)
[![](https://img.shields.io/badge/Spark-v2.4.0-blue.svg)](https://spark.apache.org)
[![](https://img.shields.io/badge/Flume-1.9.0-blue.svg)](https://flume.apache.org)
[![](https://img.shields.io/badge/Kafka-2.1.1-blue.svg)](http://kafka.apache.org)
[![](https://img.shields.io/badge/Flink-1.7.2-blue.svg)](https://flink.apache.org)

[![](https://img.shields.io/badge/Scala-2.11.12-brightgreen.svg)](https://www.scala-lang.org)
[![](https://img.shields.io/badge/Python-3.6.7-brightgreen.svg)](https://www.python.org)
[![](https://img.shields.io/badge/Java-1.8-brightgreen.svg)](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

[![](https://img.shields.io/badge/SpringBoot-2.1.3-ff69b4.svg)](https://docs.spring.io)
[![](https://img.shields.io/badge/JQuery-3.3.1-ff69b4.svg)](https://jquery.com)
[![](https://img.shields.io/badge/Sockjs-1.3.0-ff69b4.svg)](https://github.com/sockjs/sockjs-client)
[![](https://img.shields.io/badge/Stomp-2.3.3-ff69b4.svg)](http://stomp.github.io)
[![](https://img.shields.io/badge/Echarts-4.2.1-ff69b4.svg)](https://echarts.baidu.com)

## Title : Realtime Twitter Stream Analysis System

Developed By:

  - [@GaryGao](https://github.com/GaryGao829)
  - [@AlexTK2012](http://alextk2012.github.io)
  - [@BZbyr](https://github.com/BZbyr)
  - [@Yang Xiangyu](https://github.com/ulysses1881826)
  
### Project Structure
 
 ```
 .
├── CloudWeb
├── Collector
├── HBaser
├── StreamProcessorFlink
├── StreamProcessorSpark
|
└── pom.xml(Maven parent POM)

 ```
 - __CloudWeb__: 
   - Show statistics data and sentiment analysis result.
 - __Collector__:
   - Collect real-time data by Twitter Python Api.
   - Transform data to Kafka by Flume.
 - __StreamProcessorFlink__:
   - Analyze statistics from different dimensions.
 - __StreamProcessorSpark__:
   - Train and generate Naive Bayes Model.
   - Analyze sentiment of Twitter.
   
### Cluster Website

Need to connect with cs vpn.

[Spring Boot WebSite](http://202.45.128.135:20907/)

[Ganglia Cluster Monitor](http://202.45.128.135:20007/ganglia/)

[Namenode INFO](http://202.45.128.135:20107/dfshealth.html#tab-overview)

[Hadoop Application](http://202.45.128.135:20207/cluster)

[Hadoop JobHistory](http://202.45.128.135:20307/jobhistory)

[Spark History Server](http://202.45.128.135:20507/)

[Flink Server](http://202.45.128.135:20807/)

### Project Documents

[Proposal](https://docs.google.com/document/d/1zzrZSWjRAz3FpL2EyyuIOGwQPduTtCBiCcYJMfmvA4I/edit?usp=sharing)

[Meeting Record](https://docs.google.com/document/d/1NkYv8v_0XF8zxkrgxPIUUTsgPG1U0NvSgCrm8yrpxfo/edit?usp=sharing)

[Prensentation PPT](https://docs.google.com/presentation/d/13iHaXcwX7a4WkaIA79BHxhsM9GSiKSfBs25j12pviWY/edit?usp=sharing)


[地理查询 API](http://jwd.funnyapi.com/#/index)

[环境信息](https://docs.google.com/spreadsheets/d/1ikzBeQ43pcnHpoRPA4PIFMfDF4OV6SeimAASWj7pVvA/edit#gid=0)

### Related Project

 [Spark-MLlib-Twitter-Sentiment-Analysis](https://github.com/P7h/Spark-MLlib-Twitter-Sentiment-Analysis)

 [flume_kafka_spark_solr_hive](https://github.com/obaidcuet/flume_kafka_spark_solr_hive/tree/master/codes)

 [corenlp-scala-examples](https://github.com/harpribot/corenlp-scala-examples)
 
 [deeplearning4j](https://github.com/deeplearning4j/deeplearning4j)
 
 [canvas-barrage](https://github.com/zhaofinger/canvas-barrage)

### Data

 [train data](http://help.sentiment140.com/for-students)
 
 数据走向:
 
 ```
 Flume-> Kafka -> Spark Streaming -> Kafka  
         
               -> Flink -> Kafka
               
               -> DL4J -> Kafka
 ```
 
 *Flume*  将*Twitter Data* 搬运存储到 ```topic : alex1``` 供 Spark & Flink & DL4J 订阅。
 
 *Spark Streaming* 读取```topic : alex1``` 进行情感分析，存储结果数据到 ```topic : twitter-result1``` 供Web端订阅。
 
 *Cloud Web DL4J* 读取```topic : alex1``` 进行 dl4j 情感分析，结果数据不存储，直接吐到WebSocket监听的路由里，供Web端订阅。
 
 *Flink* 读取```topic : alex1``` 进行数据统计分析，
 - twitter 语言统计结果存储到```topic : twitter-flink-lang``` 供Web端订阅。
 - twitter 用户fans统计结果存储到```topic : twitter-flink-fans``` 供Web端订阅。
 - twitter 用户geo统计结果存储到```topic : twitter-flink-geo``` 供Web端订阅。
 
 数据格式:
 
 - Twitter 元数据 ```twitter4j.Status```
 - 情感分析结果 ```ID¦Name¦Text¦NLP¦MLlib¦DeepLearning¦Latitude¦Longitude¦Profile¦Date```
 - Lang 统计结果 ```{"pt":2,"ot":26,"ja":3,"en":453,"fr":12,"es":4,}```
 - Fans 统计结果 ```200|800|500~1000|above 1000```
 - map 统计结果 ```Latitude|Longitude|time```

### Operation & Conf

- [Kafka Operation](https://gist.github.com/AlexTK2012/7a1c68ec2b904528c41e726ebece4b46)

- [Flume Conf](https://gist.github.com/AlexTK2012/1d3288f0e474b4ad66db80950b402230)

- HDFS 配置项

    - Naive Bayes 模型路径 ```/tweets_sentiment/NBModel/```

    - Naive Bayes 训练/测试文件路径 ```/data/training.1600000.processed.noemoticon.csv```
    
    - Stanford Core NLP 模型路径，maven 依赖中 [stanford-corenlp-models](https://stanfordnlp.github.io/CoreNLP/download.html)
    
    - Deep Learning 模型&[词向量](https://github.com/mmihaltz/word2vec-GoogleNews-vectors/archive/master.zip)路径 ```/tweets_sentiment/dl4j/```

### Precondition

1. Make preparation for Kafka environment. We have set up kafka on total 9 machines. Create kafka topic and make sure we can consume and produce the topic.

2. Make preparation for Flume environment. We have set up flume GPU7.

3. Generate Model for Machine Learning lib, run the command and put the model on the HDFS.
```Shell
spark-submit --class "hk.hku.spark.mllib.SparkNaiveBayesModelCreator" --master local[3] /opt/spark-twitter/7305CloudProject/StreamProcessorSpark/target/StreamProcessorSpark-jar-with-dependencies.jar
```

4. The Model for CoreNLP has been stored in NLP-jars, so we only need to make sure the maven dependency is completed.

5. The Model for DL4J has been stored in the project, so we only need to make sure the [word vectors](https://github.com/mmihaltz/word2vec-GoogleNews-vectors/) (stored on HDSF) are completed.

6. The CloudWeb Spring Boot project includes DL4J and this makes it need about 6G memory. Keep mind.

7. Log in the GPU machine, `cd /opt/spark-twitter/7305CloudProject`, `git pull` code and `mvn compile` project.

### Run

1. Start *Flume* to collect twitter data and transport into *Kafka*.

```sh
# read boot_flume_sh
nohup flume-ng agent -f /opt/spark-twitter/7305CloudProject/Collector/TwitterToKafka.conf -Dflume.root.logger=DEBUG,console -n a1 >> flume.log 2>&1 &

# make sure data has been produced into kafka topic successfully
```

> We can start 3 or more flume progress to test the cluster performance.

2. Start *Spark Streaming* to analysis twitter text sentiment using stanford nlp & naive bayes.

```sh
单机模式
spark-submit --class "hk.hku.spark.TweetSentimentAnalyzer" --master local[3] /opt/spark-twitter/7305CloudProject/StreamProcessorSpark/target/StreamProcessorSpark-jar-with-dependencies.jar

集群模式
spark-submit --class "hk.hku.spark.TweetSentimentAnalyzer" --master yarn --deploy-mode cluster --num-executors 2 --executor-memory 4g --executor-cores 4 --driver-memory 4g --conf spark.kryoserializer.buffer.max=2048 --conf spark.yarn.executor.memoryOverhead=2048 /opt/spark-twitter/7305CloudProject/StreamProcessorSpark/target/StreamProcessorSpark-jar-with-dependencies.jar
```

Watch the Spark Status on the Spark History Server website.

3. Start *CloudWeb* to show the result on the [website](http://202.45.128.135:20907).

```sh
cd /opt/spark-twitter/7305CloudProject/CloudWeb/target
nohup java -Xmx3072m -jar /opt/spark-twitter/7305CloudProject/CloudWeb/target/CloudWeb-1.0-SNAPSHOT.jar & 
```

Watch the Flink Status on the Flink History Server website.

4. Start *Flink* 
```sh
flink run /opt/spark-twitter/7305CloudProject/StreamProcessorFlink/target/StreamProcessorFlink-1.0-SNAPSHOT.jar
```

### Common Issues and Solution

1. Modify Zsh Environment
```sh
# 使用zsh, 自定义环境变量需要修改:
vi ~/sh/env_zsh 
# then restart the shell
```

2. CloudWeb start failed

The most possible reason is the machine's memory isn't enough. Use command `top` to watch the status.
