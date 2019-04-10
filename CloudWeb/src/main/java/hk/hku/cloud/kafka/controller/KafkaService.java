package hk.hku.cloud.kafka.controller;

import com.google.gson.Gson;
import hk.hku.cloud.dl4j.SentimentExampleIterator;
import hk.hku.cloud.kafka.domain.SentimentTuple;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterObjectFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
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

    private static volatile boolean consumeKafka = true;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss ZZ yyyy");

    @Autowired
    private SimpMessagingTemplate template;

    // kafka consumer 配置项
    public static Properties getConsumerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "gpu7:9092,gpu7-x1:9092,gpu7-x2:9092");
        props.put("group.id", "web-consumer");
        props.put("auto.offset.reset", "latest");  //[latest(default), earliest, none]
        props.put("enable.auto.commit", "true");// 自动commit
        props.put("auto.commit.interval.ms", "1000");// 自动commit的间隔
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }

    // kafka producer 配置项
    public static Properties getProducerProperties() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "gpu7:9092,gpu7-x1:9092,gpu7-x2:9092");
        props.put("acks", "all");
        props.put("delivery.timeout.ms", 30000);
        props.put("batch.size", 16384);
//        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return props;
    }

    public void setConsumeKafka(boolean consumeKafka) {
        this.consumeKafka = consumeKafka;
        logger.info("setConsumeKafka : " + consumeKafka);
    }

    /**
     * consume kafka data 并发送到前端 /topic/consumeKafka
     */
    @Async
    public void consumeKafka() {
        Properties props = getConsumerProperties();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // 消费者订阅多个topic
        Collection<String> topics = Arrays.asList("twitter-result1");
        consumer.subscribe(topics);

        ConsumerRecords<String, String> consumerRecords;

        logger.info("Consumer Kafka start.");

        // ID, Name, Text, NLP Polarity, MLlib Polarity, DL Polarity, Latitude, Longitude, Image URL, Tweet Date.
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
                    sentimentTuple.setDlPolarity(Integer.parseInt(line[5]));
                    sentimentTuple.setLatitude(Double.parseDouble(line[6]));
                    sentimentTuple.setLongitude(Double.parseDouble(line[7]));
                    sentimentTuple.setImage(line[8]);
                    sentimentTuple.setDate(line[9]);
                } catch (Exception e) {
                    logger.error("", e);
                }

                String data = gson.toJson(sentimentTuple);
                logger.info("sendData : " + data);
                // 发送消息给订阅 "/topic/notice" 且在线的用户
                template.convertAndSend("/topic/consumeSentiment", data);
            }
        }

        consumer.close();
        // 后端断开连接时,通知一下前端，可以前端做校验---暂时不处理这种情况
        // template.convertAndSend("/topic/close", "closed");
        logger.info("Consumer Kafka End.");
    }

    /**
     * consume kafka Lang data 并发送到前端 /topic/consumeLang
     * 根据Lang语言分类
     */
    @Async
    public void consumeStatisticLang() {
        Properties props = getConsumerProperties();

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        Collection<String> topics = Arrays.asList("twitter-flink-lang");
        consumer.subscribe(topics);

        ConsumerRecords<String, String> consumerRecords;

        logger.info("Consumer Statistic Lang start.");

        while (true) {
            consumerRecords = consumer.poll(Duration.ofMillis(1000));
            logger.debug("consumerRecords count is : " + consumerRecords.count());

            for (ConsumerRecord consumerRecord : consumerRecords) {
                String value = consumerRecord.value().toString();
                template.convertAndSend("/topic/consumeLang", value);
            }
        }
    }


    /**
     * consume kafka Fans data 并发送到前端 /topic/consumeFans
     *
     */
    @Async
    public void consumeStatisticFans() {
        Properties props = getConsumerProperties();

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        Collection<String> topics = Arrays.asList("twitter-flink-fans");
        consumer.subscribe(topics);

        ConsumerRecords<String, String> consumerRecords;

        logger.info("Consumer Statistic Fans start.");

        while (true) {
            consumerRecords = consumer.poll(Duration.ofMillis(1000));
            logger.debug("consumerRecords count is : " + consumerRecords.count());

            for (ConsumerRecord consumerRecord : consumerRecords) {
                String value = consumerRecord.value().toString();
                template.convertAndSend("/topic/consumeFans", value);
            }
        }
    }

    /**
     * 定时往socket 地址发送一条消息，保证web socket 存活
     */
    @Async
    public void putSentimentTimingMessage() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
                String value = "ping-alive";
                template.convertAndSend("/topic/consumeSentiment", value);
                template.convertAndSend("/topic/consumeDeepLearning", value);
            } catch (Exception e) {
                logger.error("putTimingMessage exception : ", e);
            }
        }
    }

    /*
    同上
     */
    @Async
    public void putStatisticTimingMessage() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(3);
                String value = "ping-alive";
                template.convertAndSend("/topic/consumeLang", value);
                template.convertAndSend("/topic/consumeFans", value);
            } catch (Exception e) {
                logger.error("putTimingMessage exception : ", e);
            }
        }
    }


    /**
     * 启动Deep Learning 分析器, 加载本地存储的分析好的模型+词向量,
     * 读取kafka twitter 元数据, 进行分析并存储到kafka 中
     */
    @Async
    public void computeDL4JSentiment() {
        logger.info("compute DL4J sentiment start.");

        System.out.println("用户的当前工作目录: " + System.getProperties().getProperty("user.dir"));

        // 从本地加载模型
        MultiLayerNetwork restored = null;
        try {
            restored = ModelSerializer.restoreMultiLayerNetwork(new File("/home/hduser/dl4j/DumpedModel.zip"));
            logger.info("load model successful");
        } catch (IOException e) {
            logger.error("computeDL4JSentiment load model exception", e);
        }

        // 词向量本地路径
        String WORD_VECTORS_PATH = "/home/hduser/dl4j/GoogleNews-vectors-negative300.bin.gz";
        File wordVectorsFile = new File(WORD_VECTORS_PATH);
        // 加载词向量
        WordVectors wordVectors = WordVectorSerializer.loadStaticModel(wordVectorsFile);
        logger.info("load static model successful");

        SentimentExampleIterator iterator = new SentimentExampleIterator(wordVectors);
        logger.info("init SentimentExampleIterator");

        // 加载 kafka consumer
        Properties propsConsumer = getConsumerProperties();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(propsConsumer);
        // 消费 twitter 元数据
        Collection<String> topics = Arrays.asList("alex1");
        consumer.subscribe(topics);

        // 加载 kafka producer
//        Properties propsProducer = getProducerProperties();
//        Producer<String, String> producer = new KafkaProducer<>(propsProducer);
//        String topicsProducer = "twitter-dl4j";

        // dl4j 处理情感
        ConsumerRecords<String, String> consumerRecords;
        logger.info("Consumer Kafka start.");

        while (true) {
            consumerRecords = consumer.poll(Duration.ofMillis(3000));
            logger.info("consumerRecords count is : " + consumerRecords.count());

            for (ConsumerRecord consumerRecord : consumerRecords) {
                try {
                    String value = consumerRecord.value().toString().trim();
                    // 不可能过短
                    if (value.length() <= 5) {
                        continue;
                    }
                    Status status = TwitterObjectFactory.createStatus(value);
//                    logger.info("get twitter status : " + status.getText());

                    if (status == null || status.getText() == null || status.getText().length() < 5) {
                        continue;
                    }

                    // 计算文本的情绪值
                    INDArray features = iterator.loadFeaturesFromString(status.getText(), 256);
                    INDArray networkOutput_restored = restored.output(features);
                    long timeSeriesLength_restored = networkOutput_restored.size(2);
                    INDArray probabilitiesAtLastWord_restored = networkOutput_restored
                            .get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(timeSeriesLength_restored - 1));

                    // 吐出分析结果
                    Double positive = probabilitiesAtLastWord_restored.getDouble(0);
                    logger.info("dl4j positive : " + positive);

                    SentimentTuple sentimentTuple = new SentimentTuple();

                    // id 也以 dl4j 开头，理论上存在和spark streaming 处理的数据是同一份，导致前端弹幕滚动展示两次同一份数据的情况
                    sentimentTuple.setId(String.valueOf("dl4j" + status.getId()));
                    sentimentTuple.setName(status.getUser().getScreenName());
                    sentimentTuple.setText(status.getText());
                    sentimentTuple.setNlpPolarity(0);
                    sentimentTuple.setNbPolarity(0);

                    if (positive > 0.8)
                        sentimentTuple.setDlPolarity(1);
                    else
                        sentimentTuple.setDlPolarity(-1);

                    double latitude = -1;
                    double longtitude = -1;
                    if (status.getGeoLocation() != null) {
                        latitude = status.getGeoLocation().getLatitude();
                        longtitude = status.getGeoLocation().getLongitude();
                    }
                    sentimentTuple.setLatitude(latitude);
                    sentimentTuple.setLongitude(longtitude);
                    // 不新增字段了，直接用这无用的字段进行 dl4j 的前台校验
                    sentimentTuple.setImage("dl4j");
                    sentimentTuple.setDate(simpleDateFormat.format(status.getCreatedAt()));

//                    // 发送 dl4j 情感分析结果到kafka 中
//                    producer.send(new ProducerRecord<>(topicsProducer, status.getText()));
//                    // 感觉没必要，直接将数据吐给 websocket 就够了

                    // 发送消息给订阅 "/topic/consumeDeepLearning" 且在线的用户
                    template.convertAndSend("/topic/consumeDeepLearning", gson.toJson(sentimentTuple));

                } catch (Exception e) {
                    logger.error("TwitterException : ", e);
                }
            }
        }
    }


    //测试用
    @Async
    @Deprecated
    public void consumeKafkaTest() {
        logger.info("Test Consumer Kafka Start.");

        // ID, Name, Text, NLP Polarity, MLlib Polarity,Latitude, Longitude, Image URL, Tweet Date.
        SentimentTuple sentimentTuple = new SentimentTuple();

        while (consumeKafka) {
            try {
                TimeUnit.SECONDS.sleep(2);
                String msg = "1112703626613583872¦RachelJ_1D¦I wish the weather would act like it’s actually spring.¦-1¦1¦1¦-1.0¦-1.0¦http://pbs.twimg.com/profile_images/1082338556050317312/c5W1hGXM.jpg¦Mon Apr 01 13:09:52 +0000 2019";
                String[] line = msg.split("¦");

                sentimentTuple.setId(line[0]);
                sentimentTuple.setName(line[1]);
                sentimentTuple.setText(line[2]);
                sentimentTuple.setNlpPolarity(Integer.parseInt(line[3]));
                sentimentTuple.setNbPolarity(Integer.parseInt(line[4]));
                sentimentTuple.setDlPolarity(Integer.parseInt(line[5]));
                sentimentTuple.setLatitude(Double.parseDouble(line[6]));
                sentimentTuple.setLongitude(Double.parseDouble(line[7]));
                sentimentTuple.setImage(line[8]);
                sentimentTuple.setDate(line[9]);

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