package com.sachin.work.kafkastreams.runners;

import com.sachin.work.kafkastreams.util.GenUtil;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * .kafka-stream -f /Users/sachin/work/ws_skshukla/KafkaStreamProject/src/main/data/employee.csv -d 1000 -t mytopic -r count-stream -b
 */

@Component
public class CountStreamExample {

    private Properties props;

    @Value("${count.stream.app.id:count-stream-app}")
    private String APP_ID;

    @Value("${kafka.brokers}")
    private String KAFKA_BROKERS;

    @Value("${count.stream.topic.dept}")
    private String TOPIC_DEP;

    @Value("${count.stream.topic.dept.count}")
    private String TOPIC_DEP_COUNT;


    @Autowired
    @Qualifier("kafkaTemplateByte")
    KafkaTemplate<byte[], byte[]> kafkaTemplateByte;



    public void run() throws Exception {
        GenUtil.println(String.format("Entering method CountStreamExample#run()......" +
                " KAFKA_BROKERS {%s}, TOPIC_DEP {%s}, TOPIC_DEP_COUNT {%s}", KAFKA_BROKERS, TOPIC_DEP, TOPIC_DEP_COUNT));
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<String, String> s = streamsBuilder.stream(TOPIC_DEP, Consumed.with(Serdes.String(), Serdes.String()));
        GenUtil.println(String.format("Starting read from topic {%s}", TOPIC_DEP));

        KGroupedStream<Integer, String> s2 = s.selectKey( (k, v) -> {
            String rating = new org.json.JSONObject(v).getString("dep_rating");
            return Integer.parseInt(rating);
        }).groupByKey(Serialized.with(Serdes.Integer(), Serdes.String()));

        s2.count().toStream().foreach((k, v) -> {
            GenUtil.println(String.format("Reduced for count: key {%s}, val {%s}", k, v));
        });

        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), loadProperties());
        streams.start();
        GenUtil.println("Going to sleep!!!!");
        GenUtil.sleep(5 * 60 * 1000);
        GenUtil.println("Exiting method CountStreamExample#run()......");
        streams.close();
    }


    private synchronized Properties loadProperties() {
        if (props != null) {
            return props;
        }
        props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 2);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        return props;
    }
}
