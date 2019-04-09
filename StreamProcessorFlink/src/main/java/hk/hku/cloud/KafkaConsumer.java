package hk.hku.cloud;
/**
 * Created by IntelliJ IDEA
 * Project: CloudProject
 * Author: Boyang
 * Date: 2019-04-09 11:45
 */
import com.google.gson.Gson;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.FoldFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.util.*;
/**
 *
 *
 * @package: hk.hku.cloud
 * @class: KafkaConsumer
 * @author: Boyang
 * @date: 2019-04-09 11:45
 */
public class KafkaConsumer {
    private static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    private static final String GROUP_ID = "group.id";
    private static final String KAFKA_TOPIC = "kafka.topic";

    private static final String BOOTSTRAP_SERVERS_PRODUCER = "bootstrap.servers.producer";
    private static final String GROUP_ID_PRODUCER = "group.id.producer";
    private static final String KAFKA_TOPIC_PRODUCER_1 = "kafka.topic.producer1";
    private static final String KAFKA_TOPIC_PRODUCER_2 = "kafka.topic.producer2";
    private static final String KAFKA_TOPIC_PRODUCER_3 = "kafka.topic.producer3";

    private static final Map<String, Long> langMap = new HashMap<>();
    public static Gson gson = new Gson();
    public static void run(Context context) throws Exception {
        // set up the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties propConsumer = new Properties();
        propConsumer.setProperty("bootstrap.servers", context.getString(BOOTSTRAP_SERVERS));
        propConsumer.setProperty("group.id", context.getString(GROUP_ID));

        Properties propProducer = new Properties();
        propProducer.setProperty("bootstrap.servers", context.getString(BOOTSTRAP_SERVERS_PRODUCER));
        propProducer.setProperty("group.id", context.getString(GROUP_ID_PRODUCER));



        // get input data
        DataStream<String> stream = env.addSource(new FlinkKafkaConsumer<>(context.getString(KAFKA_TOPIC), new SimpleStringSchema(), propConsumer));

        // Stream transformations
        DataStream<Status> tweets = stream.map(new JSONParser()).filter(tweet -> ((tweet.getCreatedAt() != null) && (tweet.getText() != null)));

        // create a stream of GPS information
        DataStream<String> geoInfo =
                tweets.filter(tweet -> TweetFunctions.getTweetGPSCoordinates(tweet) != null)
                        .map(new TweetToLocation());
        //write GPS information into kafka topic3
        geoInfo.addSink(new FlinkKafkaProducer<String>(context.getString(KAFKA_TOPIC_PRODUCER_3), new SimpleStringSchema(), propProducer));


        DataStream<LangWithCount> countsLang = tweets.filter(tweet -> (TweetFunctions.getTweetLanguage(tweet) != null)).flatMap(new TweetToLang()).keyBy("lang").countWindow(3000,300).sum("count");
        DataStream<String> langString = countsLang.keyBy("lang").fold(langMap,new FoldFunction<LangWithCount, Map<String, Long>>() {

            @Override
            public Map<String, Long> fold(Map<String, Long> current, LangWithCount value) {
                current.put(value.getLang(),value.getCount());
                return current;
            }
        }).map(new MapFunction<Map<String, Long>, String>() {
            @Override
            public String map(Map<String, Long> value) throws Exception {
                return gson.toJson(value);
            }
        });
        langString.addSink(new FlinkKafkaProducer<String>(context.getString(KAFKA_TOPIC_PRODUCER_1), new SimpleStringSchema(), propProducer));



        // execute program
        env.execute("Java Flink KafkaConsumer");
    }

    public static void main(String[] args) {
        try {
            String configFileLocation = "/opt/spark-twitter/7305CloudProject/StreamProcessorFlink/src/main/resources/kafka.properties";
            Context context = new Context(configFileLocation);
            KafkaConsumer.run(context);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }


    /**
     * Maps a tweet to its country, latitude, longitude, and timestamp
     *
     * @package: hk.hku.cloud
     * @class: KafkaConsumer
     * @author: Boyang
     * @date: 2019-04-09 19:10
     */
    public static class TweetToLocation implements MapFunction<Status, String> {
        @Override
        public String map(Status tweet) throws Exception {
            return TweetFunctions.getTweetGPSCoordinates(tweet).getLatitude() + "|"
                    + TweetFunctions.getTweetGPSCoordinates(tweet).getLongitude() + "|"
                    + tweet.getCreatedAt().getTime();
        }
    }

    /**
     * Maps a tweet to lang, count
     */
    public static class TweetToLang implements FlatMapFunction<Status, LangWithCount> {

        private static final long serialVersionUID = 1L;

        @Override
        public void flatMap(Status tweet, Collector<LangWithCount> out) {
            String lang = TweetFunctions.getTweetLanguage(tweet);
            out.collect(new LangWithCount(lang,1L));
        }


    }

    /**
     * Implements the JSON parser provided by twitter4J into Flink MapFunction
     */
    public static final class JSONParser implements MapFunction<String, Status> {
        @Override
        public Status map(String value) {
            Status status = null;
            try {
                status = TwitterObjectFactory.createStatus(value);
            } catch(TwitterException e) {

            } finally {
                // return the parsed tweet, or null if exception occured
                return status;
            }
        }
    }

    /**
     * 主要为了存储单词以及单词出现的次数
     */
    public static class LangWithCount{
        public String lang;
        public long count;
        public LangWithCount(){}
        public LangWithCount(String lang, long count) {
            this.lang = lang;
            this.count = count;
        }

        public long getCount() {
            return count;
        }

        public String getLang() {
            return lang;
        }

        @Override
        public String toString() {
            return "LangWithCount{" +
                    "lang='" + lang + '\'' +
                    ", count=" + count +
                    '}';
        }
    }
}