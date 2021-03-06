package hk.hku.cloud;
/**
 * Created by IntelliJ IDEA
 * Project: CloudProject
 * Author: Boyang
 * Date: 2019-04-09 11:45
 */

import com.google.gson.Gson;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.ProcessAllWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.util.*;

/**
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

    public static Gson gson = new Gson();
    private static Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);

    private static Map<String, Long> langMap = new HashMap<>();
    public static void run(Context context) throws Exception {
        // set up the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties propConsumer = new Properties();
        propConsumer.setProperty("bootstrap.servers", context.getString(BOOTSTRAP_SERVERS));
        propConsumer.setProperty("group.id", context.getString(GROUP_ID));

        Properties propProducer = new Properties();
        propProducer.setProperty("bootstrap.servers", context.getString(BOOTSTRAP_SERVERS_PRODUCER));
        propProducer.setProperty("group.id", context.getString(GROUP_ID_PRODUCER));

        TypeInformation<Tuple2<Status, String>> typeInformation = TypeInformation.of(new TypeHint<Tuple2<Status, String>>() {});
        // get input data
        DataStream<String> stream =
                env.addSource(new FlinkKafkaConsumer<>(context.getString(KAFKA_TOPIC), new SimpleStringSchema(), propConsumer))
                    .name("KafkaConsumer");

        // Stream transformations
        DataStream<Status> tweets =
                stream.filter(line -> line.toString().trim().length() > 0)
                        .map(new JsonParser())
                        .filter(tweet -> (tweet != null && (tweet.getCreatedAt() != null) && (tweet.getText() != null)))
                        .name("Tweets Sweeper");

        // create a stream of GPS information
        DataStream<String> geoInfo =
                tweets.filter(tweet -> TweetFunctions.getTweetGPSCoordinates(tweet) != null)
                        .map(new TweetToLocation())
                        .name("Tweets to Location");

        // language count
        DataStream<Map<String,Long>> countsLang =
                tweets.filter(tweet -> (TweetFunctions.getTweetLanguage(tweet) != null))
                        .map(new TweetWithLang())
                        .returns(typeInformation)
                        .countWindowAll(100,80)
                        .process(new ProcessAllWindowFunction<Tuple2<Status, String>, Map<String,Long>, GlobalWindow>() {
                            @Override
                            public void process(Context context, Iterable<Tuple2<Status, String>> elements, Collector<Map<String,Long>> out) throws Exception {
                                long zhCount = 0;
                                long enCount = 0;
                                long jaCount = 0;
                                long esCount = 0;
                                long ptCount = 0;
                                long arCount = 0;
                                long frCount = 0;
                                long koCount = 0;
                                long otCount = 0;
                                langMap.clear();
                                for (Tuple2<Status, String> element : elements) {
                                    switch (element.f1){
                                        case "zh":
                                            zhCount ++;
                                            langMap.put("zh",zhCount);
                                            break;
                                        case "en":
                                            enCount ++;
                                            langMap.put("en",enCount);
                                            break;
                                        case "ja":
                                            jaCount ++;
                                            langMap.put("ja",jaCount);
                                            break;
                                        case "es":
                                            esCount ++;
                                            langMap.put("es",esCount);
                                            break;
                                        case "pt":
                                            ptCount ++;
                                            langMap.put("pt",ptCount);
                                            break;
                                        case "ar":
                                            arCount ++;
                                            langMap.put("ar",arCount);
                                            break;
                                        case "fr":
                                            frCount ++;
                                            langMap.put("fr",frCount);
                                            break;
                                        case "ko":
                                            koCount ++;
                                            langMap.put("ko",koCount);
                                            break;
                                        case "ot":
                                            otCount ++;
                                            langMap.put("ot",otCount);
                                            break;

                                        default:
                                            break;

                                    }
                                }
                                out.collect(langMap);
                            }
                        })
                        .name("Language Count Map Factory");
        DataStream<String> langString =
                countsLang.map(new MapFunction<Map<String, Long>, String>() {
                                @Override
                                public String map(Map<String, Long> value) throws Exception {
                                    return gson.toJson(value);
                                }
                            })
                            .name("Language Count Json String Factory");

        DataStream<String> countsByFollowers =
                tweets.filter(tweet -> (TweetFunctions.getUsrFollowerNumLevel(tweet) != null))
                        .map(new TweetWithFollowersLevel())
                        .returns(typeInformation)
                        .countWindowAll(100,80)
                        .process(
                                new ProcessAllWindowFunction<Tuple2<Status, String>, String, GlobalWindow>() {
                                     @Override
                                     public void process(Context context, Iterable<Tuple2<Status, String>> elements, Collector<String> out) throws Exception {
                                        long count200 = 0;
                                        long count800 = 0;
                                        long count2k = 0;
                                        long count5k = 0;
                                        long count20k = 0;
                                        long count100k = 0;
                                        long count1kk = 0;
                                        long count1kkp = 0;
                                        for (Tuple2<Status,String> element:elements){
                                            switch (element.f1){
                                                case "200":
                                                    count200 ++;
                                                    break;
                                                case "800":
                                                    count800 ++;
                                                    break;
                                                case "2k":
                                                    count2k ++;
                                                    break;
                                                case "5k":
                                                    count5k ++;
                                                    break;
                                                case "20k":
                                                    count20k ++;
                                                    break;
                                                case "100k":
                                                    count100k ++;
                                                    break;
                                                case "1kk":
                                                    count1kk ++;
                                                    break;
                                                case "1kk+":
                                                    count1kkp ++;
                                                    break;

                                                default:
                                                    break;
                                            }
                                        }
                                        out.collect(new String(count200+"|"+count800+"|"+count2k+"|"+count5k+"|"+count20k+"|"+count100k+"|"+count1kk+"|"+count1kkp));
                                     }
                                }
                        ).name("followerLevel count");

        //write GPS information into kafka topic3
        geoInfo.addSink(new FlinkKafkaProducer<String>(context.getString(KAFKA_TOPIC_PRODUCER_3), new SimpleStringSchema(), propProducer))
                .name("Location Sink");

        langString.addSink(new FlinkKafkaProducer<String>(context.getString(KAFKA_TOPIC_PRODUCER_1), new SimpleStringSchema(), propProducer))
                .name("Language Count Sink");

        countsByFollowers.addSink(new FlinkKafkaProducer<String>(context.getString(KAFKA_TOPIC_PRODUCER_2), new SimpleStringSchema(),propProducer))
                            .name("follower level sink");




        // execute program
        env.execute("7305 Java Flink KafkaConsumer");
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
     * Maps a tweet to Tuple< tweet, lang >
     */
    public static class TweetWithLang implements MapFunction<Status, Tuple2<Status,String>> {

        private static final long serialVersionUID = 1L;

        @Override
        public Tuple2<Status,String> map(Status tweet) {
            return new Tuple2<>(tweet, TweetFunctions.getTweetLanguage(tweet));
        }
    }

    public static class TweetWithFollowersLevel implements MapFunction<Status,Tuple2<Status,String>>{
        private static final long serialVersionUID= 1L;

        @Override
        public Tuple2<Status, String> map(Status tweet){
            return new Tuple2<>(tweet,TweetFunctions.getUsrFollowerNumLevel(tweet));
        }
    }

    /**
     * Implements the JSON parser provided by twitter4J into Flink MapFunction
     */
    public static final class JsonParser implements MapFunction<String, Status> {
        @Override
        public Status map(String value) {
            Status status = null;
            try {
                status = TwitterObjectFactory.createStatus(value);
            } catch (TwitterException e) {
                logger.error("TwitterException : ", e);
            } finally {
                // return the parsed tweet, or null if exception occured
                return status;
            }
        }
    }

    /**
     * 统计每个 level 的发帖量
     */
    public static class WordWithCount{
        private String level;
        private long count;
        public WordWithCount(){}
        public WordWithCount(String word, long count) {
            this.level = word;
            this.count = count;
        }

        public long getCount() {
            return count;
        }

        public String getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return "WordWithCount{" +
                    "word='" + level + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

}