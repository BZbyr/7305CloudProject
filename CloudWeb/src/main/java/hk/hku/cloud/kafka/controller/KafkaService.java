package hk.hku.cloud.kafka.controller;

import com.google.gson.Gson;
import hk.hku.cloud.kafka.domain.SentimentTuple;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author: LexKaing
 * @create: 2019-04-02 02:22
 * @description:
 **/
@Service
public class KafkaService {
    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    private static Gson gson = new Gson();

    private volatile boolean consumeKafka = true;

    @Autowired
    private SimpMessagingTemplate template;

    public void setConsumeKafka(boolean consumeKafka) {
        this.consumeKafka = consumeKafka;
        logger.info("setConsumeKafka : " + consumeKafka);
    }

    /**
     * consume kafka data 并发送到前端 /topic/consumeKafka
     */
    @Async
    public void consumeKafka() {
        //配置项
        Properties props = new Properties();

        props.put("bootstrap.servers", "202.45.128.135:29107");
        props.put("group.id", "web-consumer");
        props.put("auto.offset.reset", "latest");  //[latest(default), earliest, none]
        props.put("enable.auto.commit", "true");// 自动commit
        props.put("auto.commit.interval.ms", "1000");// 自动commit的间隔
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // 消费者订阅多个topic
        Collection<String> topics = Arrays.asList("twitter-result1");
        consumer.subscribe(topics);

        ConsumerRecords<String, String> consumerRecords;

        logger.info("Consumer Kafka start.");

        // ID, Name, Text, NLP Polarity, MLlib Polarity,Latitude, Longitude, Image URL, Tweet Date.
        SentimentTuple sentimentTuple = new SentimentTuple();

        while (consumeKafka) {
            // 从topic中拉取数据:
            // timeout(ms): buffer 中的数据未就绪情况下，等待的最长时间，如果设置为0，立即返回 buffer 中已经就绪的数据
            consumerRecords = consumer.poll(Duration.ofMillis(1000));
            logger.info("consumerRecords count is : " + consumerRecords.count());

            // 遍历每一条记录--handle records
            for (ConsumerRecord consumerRecord : consumerRecords) {
                Object key = consumerRecord.key();
                String value = consumerRecord.value().toString();
                String[] line = value.split("¦");

                try {
                    sentimentTuple.setId(line[0]);
                    sentimentTuple.setName(line[1]);
                    sentimentTuple.setText(line[2]);
                    sentimentTuple.setNlpPolarity(Integer.parseInt(line[3]));
                    sentimentTuple.setNbPolarity(Integer.parseInt(line[4]));
                    sentimentTuple.setLatitude(Double.parseDouble(line[5]));
                    sentimentTuple.setLongitude(Double.parseDouble(line[6]));
                    sentimentTuple.setImage(line[7]);
                    sentimentTuple.setDate(line[8]);
                } catch (Exception e) {
                    logger.error("", e);
                }

                String data = gson.toJson(sentimentTuple);
                logger.info("sendData : " + data);
                // 发送消息给订阅 "/topic/notice" 且在线的用户
                template.convertAndSend("/topic/consumeKafka", data);
            }
        }

        consumer.close();
        logger.info("Consumer Kafka End.");
    }

    @Async
    public void consumeKafkaTest() {
        logger.info("Test Consumer Kafka Start.");

        // ID, Name, Text, NLP Polarity, MLlib Polarity,Latitude, Longitude, Image URL, Tweet Date.
        SentimentTuple sentimentTuple = new SentimentTuple();

        while (consumeKafka) {
            try {
                TimeUnit.SECONDS.sleep(5);
                String msg = "1112703626613583872¦RachelJ_1D¦I wish the weather would act like it’s actually spring.¦-1¦-1¦-1.0¦-1.0¦http://pbs.twimg.com/profile_images/1082338556050317312/c5W1hGXM.jpg¦Mon Apr 01 13:09:52 +0000 2019";
                String[] line = msg.split("¦");

                sentimentTuple.setId(line[0]);
                sentimentTuple.setName(line[1]);
                sentimentTuple.setText(line[2]);
                sentimentTuple.setNlpPolarity(Integer.parseInt(line[3]));
                sentimentTuple.setNbPolarity(Integer.parseInt(line[4]));
                sentimentTuple.setLatitude(Double.parseDouble(line[5]));
                sentimentTuple.setLongitude(Double.parseDouble(line[6]));
                sentimentTuple.setImage(line[7]);
                sentimentTuple.setDate(line[8]);

            } catch (Exception e) {
                logger.error("", e);
            }

            String data = gson.toJson(sentimentTuple);
            logger.info("Test Consumer Kafka, id : " + sentimentTuple.getId());
            // 发送消息给订阅 "/topic/notice" 且在线的用户
            template.convertAndSend("/topic/consumeKafka", data);
        }

        logger.info("Test Consumer Kafka End.");
    }
}