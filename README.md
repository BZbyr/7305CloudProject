# CloudProject

### Environment

[![](https://img.shields.io/badge/Hadoop-v2.7.5-blue.svg)](https://hadoop.apache.org)
[![](https://img.shields.io/badge/Spark-v2.4.0-blue.svg)](https://spark.apache.org)
[![](https://img.shields.io/badge/Flume-1.9.0-blue.svg)](https://flume.apache.org)
[![](https://img.shields.io/badge/Kafka-2.1.1-blue.svg)](http://kafka.apache.org)
[![](https://img.shields.io/badge/Flink-1.7.2-blue.svg)](https://flink.apache.org)
[![](https://img.shields.io/badge/Scala-2.11.12-brightgreen.svg)](https://www.scala-lang.org)
[![](https://img.shields.io/badge/Python-3.6.7-brightgreen.svg)](https://www.python.org)

Term Project for **COMP7305 Cluster and Cloud Computing**.

## Title : Realtime Twitter Stream Analysis System

Developed By:

  - [@GaryGao](https://github.com/GaryGao829)
  - [@lexkaing](https://github.com/AlexTK2012)
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
 - __Collector__:
   - Collect data from Twitter
 - __HBaser__:
   - a Kafka-HBase Connector
 - __StreamProcessorFlink__:
 - __StreamProcessorSpark__:

### Cluster Website

Need to connect with cs vpn.

[WebSite](http://202.45.128.135:20907/)

[Namenode INFO](http://202.45.128.135:20107/dfshealth.html#tab-overview)

[Hadoop Application](http://202.45.128.135:20207/cluster)

[Hadoop JobHistory](http://202.45.128.135:20307/jobhistory)

[Spark](http://202.45.128.135:20507/)

### Project Documents

[Proposal](https://docs.google.com/document/d/1zzrZSWjRAz3FpL2EyyuIOGwQPduTtCBiCcYJMfmvA4I/edit?usp=sharing)

[Meeting Record](https://docs.google.com/document/d/1NkYv8v_0XF8zxkrgxPIUUTsgPG1U0NvSgCrm8yrpxfo/edit?usp=sharing)

[地理查询 API](http://jwd.funnyapi.com/#/index)

### Related Project

 [Spark-MLlib-Twitter-Sentiment-Analysis](https://github.com/P7h/Spark-MLlib-Twitter-Sentiment-Analysis)

 [flume_kafka_spark_solr_hive](https://github.com/obaidcuet/flume_kafka_spark_solr_hive/tree/master/codes)

 [corenlp-scala-examples](https://github.com/harpribot/corenlp-scala-examples)

### Data

 [train data](http://help.sentiment140.com/for-students)

### Operation

1. Start *Flume* to collect twitter data and transport into *Kafka*.

  ```sh
  # read boot_flume_sh
  nohup flume-ng agent -f /opt/spark-twitter/7305CloudProject/Collector/TwitterToKafka.conf -Dflume.root.logger=DEBUG,console -n a1
  ```

2. Start *Spark Streaming* to analysis twitter text sentiment using stanford nlp & naive bayes.

```sh
spark-submit --class "hk.hku.spark.TweetSentimentAnalyzer" --master local[3] /opt/spark-twitter/7305CloudProject/StreamProcessorSpark/target/StreamProcessorSpark-jar-with-dependencies.jar
```

3. Start *CloudWeb* to show the result on the [website](http://202.45.128.135:20907).

```sh
nohup java -jar /opt/spark-twitter/7305CloudProject/CloudWeb/target/CloudWeb-1.0-SNAPSHOT.jar &
```
