package com.sachin.work.kafkastreams.runners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.sachin.work.kafkastreams.serdes.JsonDeserializer;
import com.sachin.work.kafkastreams.serdes.JsonSerializer;
import com.sachin.work.kafkastreams.util.GenUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
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
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

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

    @Value("${count.stream.topic.employee}")
    private String TOPIC_EMP;

    @Value("${count.stream.topic.employee.count}")
    private String TOPIC_EMP_COUNT;


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


    public void run_Employee() throws Exception {
//        pushEmpDataToTopicAsync();

        GenUtil.println(String.format("Entering method CountStreamExample#run_Employee()......" +
                " KAFKA_BROKERS {%s}, TOPIC_EMP {%s}, TOPIC_EMP_COUNT {%s}", KAFKA_BROKERS, TOPIC_EMP, TOPIC_EMP_COUNT));
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<byte[], Employee> ks = streamsBuilder.stream(TOPIC_EMP, Consumed.with(Serdes.ByteArray(), getEmpSerde()));
        GenUtil.println(String.format("Starting read from topic {%s}", TOPIC_EMP));

        ks.foreach( (k, v) -> {
            GenUtil.println(String.format("Orig content from topic {%s} is : {%s}", TOPIC_EMP, v));
        });

        KTable<String, Long> ktEmpCountByDeptId = ks.selectKey( (k, v) -> String.valueOf(v.getDeptId()))
                .groupByKey(Serialized.with(Serdes.String(), getEmpSerde())).count();



        ktEmpCountByDeptId.toStream().mapValues((v) -> String.format("val is : {%d}", v)).to(TOPIC_EMP_COUNT, Produced.with(Serdes.String(), Serdes.String()));

        ktEmpCountByDeptId.toStream().foreach( (k,v ) -> {
            GenUtil.println(String.format("For Dep id {%s}, Number of employees are {%d}", k, v));
        });

        KafkaStreams streams = new KafkaStreams(streamsBuilder.build(), loadProperties());
        streams.start();
        GenUtil.println("Going to sleep!!!!");
        GenUtil.sleep(5 * 60 * 1000);
        GenUtil.println("Exiting method CountStreamExample#run()......");
        streams.close();
    }





    private void pushEmpDataToTopicAsync() throws Exception{
        final String empCSV = "data/employee.csv"; // This path would work from script only as its the path in jar file
        final InputStream empCSVIS = getClass().getClassLoader().getResourceAsStream(empCSV);
        final List<String> fileContents = GenUtil.readFromInputStream(empCSVIS);
        final ObjectMapper mapper = new ObjectMapper();
        final Gson gson = new Gson();
        final int sleepInMs = 500;


        Executors.newSingleThreadExecutor().submit( () -> {

            fileContents.stream().map(e -> {
                GenUtil.println(String.format("To Topic : {%s}, Sending data : {%s}", TOPIC_EMP, e));
                return gson.fromJson(e, Employee.class);
            }).forEach(e -> {
                kafkaTemplateByte.send(TOPIC_EMP, null, gson.toJson(e).getBytes(StandardCharsets.UTF_8));
                GenUtil.sleep(sleepInMs);
            } );
        });
        GenUtil.sleep(10 * 1000);
    }

    private static Serde<Employee> getEmpSerde() {
        JsonSerializer<Employee> serializer = new JsonSerializer<>();
        JsonDeserializer<Employee> deserializer = new JsonDeserializer<>(Employee.class);
        return Serdes.serdeFrom(serializer, deserializer);
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

@Data
@NoArgsConstructor
@AllArgsConstructor
class Employee  implements Serializable {
    private int id;
    private String gender;
    private String name;
    private int deptId;

    public String toString() {
        return String.format("id {%d}, gender {%s}, name {%s}, deptId {%d}", id, gender, name, deptId);
    }
}
