package hk.hku.cloud;
/**
 * Created by IntelliJ IDEA
 * Project: CloudProject
 * Author: Boyang
 * Date: 2019-04-09 20:54
 */
import java.util.Properties;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.apache.flink.util.Collector;

public class WordCount {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.enableCheckpointing(1000);

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "192.168.1.20:9092");
        properties.setProperty("zookeeper.connect", "192.168.1.20:2181");
        properties.setProperty("group.id", "test");

        FlinkKafkaConsumer<String> myConsumer = new FlinkKafkaConsumer<String>("test", new SimpleStringSchema(),
                properties);

        DataStream<String> stream = env.addSource(myConsumer);

        DataStream<WordWithCount> counts = stream.flatMap(new LineSplitter()).keyBy("word").sum("count");

        counts.print();

        env.execute("WordCount from Kafka data");
    }

    public static final class LineSplitter implements FlatMapFunction<String, WordWithCount> {
        private static final long serialVersionUID = 1L;

        @Override
        public void flatMap(String lang, Collector<WordWithCount> out) {
            out.collect(new WordWithCount("zh",1L));
            out.collect(new WordWithCount("en",1L));
            out.collect(new WordWithCount("ja",1L));
            out.collect(new WordWithCount("es",1L));
            out.collect(new WordWithCount("ms",1L));
            out.collect(new WordWithCount("pt",1L));
            out.collect(new WordWithCount("ar",1L));
            out.collect(new WordWithCount("fr",1L));
            out.collect(new WordWithCount("tr",1L));
            out.collect(new WordWithCount("ko",1L));
            out.collect(new WordWithCount("ot",1L));
        }
    }

    /**
     * 主要为了存储单词以及单词出现的次数
     */
    public static class WordWithCount{
        public String word;
        public long count;
        public WordWithCount(){}
        public WordWithCount(String lang, long count) {
            this.word = lang;
            this.count = count;
        }

        @Override
        public String toString() {
            return "WordWithCount{" +
                    "word='" + word + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

    public static class

}