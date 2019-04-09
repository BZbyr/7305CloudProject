package hk.hku.cloud;
/**
 * Created by IntelliJ IDEA
 * Project: CloudProject
 * Author: Boyang
 * Date: 2019-04-09 11:45
 */
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple4;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
                tweets.filter(tweet -> (TweetFunctions.getTweetCountry(tweet) != null))
                        .filter(tweet -> TweetFunctions.getTweetGPSCoordinates(tweet) != null)
                        .map(new TweetToLocation());
        //geoInfo.print();
        geoInfo.addSink(new FlinkKafkaProducer<String>(context.getString(KAFKA_TOPIC_PRODUCER_3), new SimpleStringSchema(), propProducer));

        // create a stream of Sentiment Analysis
        DataStream<Tuple2<Long, String>> sentimentInfo = tweets.map(new TweetToSentiment());
        //sentimentInfo.print();
        sentimentInfo.addSink(new ElasticsearchSink<>(config, transportAddresses, new SentimentInserter()));

        // create a stream of hashtags
        DataStream<Tuple2<Long, String>> hashtagsInfo = tweets.filter(tweet -> TweetFunctions.getHashtags(tweet).length > 0)
                .map(tweet -> new Tuple2<>(tweet.getCreatedAt().getTime(), TweetFunctions.getHashtagsAsString(tweet)))
                .returns(new TypeHint<Tuple2<Long, String>>() {});
        hashtagsInfo.addSink(new ElasticsearchSink<>(config, transportAddresses, new HashtagsInserter()));

        // create a stream of keywords
        DataStream<Tuple2<Long, String>> keywordsInfo = tweets.filter(tweet -> TweetFunctions.getKeywords(tweet).length > 0)
                .map(tweet -> new Tuple2<>(tweet.getCreatedAt().getTime(), TweetFunctions.getKeywordsAsString(tweet)))
                .returns(new TypeHint<Tuple2<Long, String>>() {});
        keywordsInfo.addSink(new ElasticsearchSink<>(config, transportAddresses, new KeywordsInserter()));

        // execute program
        env.execute("Java Flink KafkaConsumer");
    }

    public static void main(String[] args) {
        if (args.length != 1){
            System.err.println("USAGE:\nKafkaConsumer <configFilePath>");
            return;
        }
        try {
            String configFileLocation = "kafka.properties";
            Context context = new Context(configFileLocation);
            KafkaConsumer.run(context);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Inserts tweet sentiments into elasticsearch.
     */
    public static class SentimentInserter implements ElasticsearchSinkFunction<Tuple2<Long, String>> {

        // construct index request
        @Override
        public void process(
                Tuple2<Long, String> record,
                RuntimeContext ctx,
                RequestIndexer indexer) {

            // construct JSON document to index
            Map<String, String> json = new HashMap<>();
            json.put("time", record.f0.toString());         // timestamp
            json.put("sentiment", record.f1);      // sentiment

            IndexRequest rqst = Requests.indexRequest()
                    .index("sentiments")        // index name
                    .type("sentimentsType")  // mapping name
                    .source(json);

            indexer.add(rqst);
        }
    }

    /**
     * Inserts tweet hashtags into the "twitter-analytics" index.
     */
    public static class HashtagsInserter implements ElasticsearchSinkFunction<Tuple2<Long, String>> {

        // construct index request
        @Override
        public void process(
                Tuple2<Long, String> record,
                RuntimeContext ctx,
                RequestIndexer indexer) {

            // construct JSON document to index
            Map<String, String> json = new HashMap<>();
            json.put("time", record.f0.toString());         // timestamp
            json.put("hashtags", record.f1);      // hashtags

            IndexRequest rqst = Requests.indexRequest()
                    .index("hashtags")        // index name
                    .type("hashtagsType")  // mapping name
                    .source(json);

            indexer.add(rqst);
        }
    }

    /**
     * Inserts tweet keywords into the "twitter-analytics" index.
     */
    public static class KeywordsInserter implements ElasticsearchSinkFunction<Tuple2<Long, String>> {

        // construct index request
        @Override
        public void process(
                Tuple2<Long, String> record,
                RuntimeContext ctx,
                RequestIndexer indexer) {

            // construct JSON document to index
            Map<String, String> json = new HashMap<>();
            json.put("time", record.f0.toString());         // timestamp
            json.put("keywords", record.f1);      // keywords

            IndexRequest rqst = Requests.indexRequest()
                    .index("keywords")        // index name
                    .type("keywordsType")  // mapping name
                    .source(json);

            indexer.add(rqst);
        }
    }


    /**
     * Maps a tweet to its country, latitude, longitude, and timestamp
     */
    public static class TweetToLocation implements MapFunction<Status, String> {
        @Override
        public String map(Status tweet) throws Exception {
            return new String(TweetFunctions.getTweetCountry(tweet)+"|"
                    +TweetFunctions.getTweetGPSCoordinates(tweet).getLatitude()+"|"
                    +TweetFunctions.getTweetGPSCoordinates(tweet).getLongitude()+"|"
                    +tweet.getCreatedAt().getTime()
            );
        }
    }

    /**
     * Maps a tweet to its country, latitude, longitude, and timestamp
     */
    public static class TweetToSentiment implements MapFunction<Status, Tuple2<Long, String>>, CheckpointedFunction {

        private transient ListState<BasicSentimentAnalysis> modelState;

        private transient BasicSentimentAnalysis model;

        @Override
        public Tuple2<Long,String> map(Status tweet) throws Exception {
            Long time = tweet.getCreatedAt().getTime();
            String text = tweet.getText();
            String score = this.model.getSentimentLabel(text);
            return new Tuple2<>(time, score);
        }

        @Override
        public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
            // constant model, so nothing to do
        }

        @Override
        public void initializeState(FunctionInitializationContext context) throws Exception {
            ListStateDescriptor<BasicSentimentAnalysis> listStateDescriptor = new ListStateDescriptor<>("model", BasicSentimentAnalysis.class);

            modelState = context.getOperatorStateStore().getUnionListState(listStateDescriptor);

            if (context.isRestored()) {
                // restore the model from state
                model = modelState.get().iterator().next();
            } else {
                modelState.clear();

                // read the model from somewhere, e.g. read from a file
                model = new BasicSentimentAnalysis("negative-words.txt", "positive-words.txt");

                // update the modelState so that it is checkpointed from now
                modelState.add(model);
            }
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
                // TODO: use LOGGER class and add a small explaining message
                e.printStackTrace();
            } finally {
                return status; // return the parsed tweet, or null if exception occured
            }
        }
    }
}