package hk.hku.cloud;
/**
 * Created by IntelliJ IDEA
 * Project: CloudProject
 * Author: Boyang
 * Date: 2019-04-08 20:15
 */

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
/**
 *
 *
 * @package: hk.hku.cloud
 * @class: ReadFromKafka
 * @author: Boyang
 * @date: 2019-04-08 20:15
 */
public class ReadFromKafka {

    private static Logger LOG = LoggerFactory.getLogger(ReadFromKafka.class);
    public static void main(String[] args) throws Exception{
        //create execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "gpu7:9092,gpu7-x1:9092,gpu7-x2:9092");
        properties.setProperty("group.id", "flink-consumer");
        DataStream<String> stream = env.addSource(new FlinkKafkaConsumer<>("alex1", new SimpleStringSchema(), properties));

        LOG.info("Read: start.");

        stream.map(new MapFunction<String, String>() {
            private static final long serialVersionUID = -6867736771747690202L;

            @Override
            public String map(String value) throws Exception {
                return "Stream Value: " + value;
            }
        }).print();

        env.execute();
        LOG.info("Read: end.");
    }
}
