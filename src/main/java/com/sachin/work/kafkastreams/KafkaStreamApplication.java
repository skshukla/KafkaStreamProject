package com.sachin.work.kafkastreams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.opencsv.CSVReader;
import com.sachin.work.kafkastreams.util.GenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileReader;
import java.time.Duration;
import java.util.*;
import java.util.stream.IntStream;

@SpringBootApplication
public class KafkaStreamApplication {
    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamApplication.class, args);
    }
}

@Component
class Sample {



//    @Value("${file:src/main/data/dummy_stream_user_data.csv}")
    @Value("${file:/tmp/data.csv}")
    private String filePath;

    @Value("${topic:t-002}")
    private String topic;

    @Value("${delay:1000}")
    private String delayInMs;


    private ApplicationArguments appArgs;

    public Sample(ApplicationArguments appArgs) {
        this.appArgs = appArgs;
    }

    @Autowired
    KafkaTemplate<byte[], byte[]> kafkaTemplate;


    @EventListener(ApplicationReadyEvent.class)
    public void start() throws Exception {
        GenUtil.println(String.format("Starting KafkaStream application with filePath {%s}, topic {%s}, delayInMs {%s}", filePath, topic, delayInMs));
        final List<byte[]> dataBytes = getCSVDataAsBytes(filePath);

        dataBytes.forEach(e -> {
            GenUtil.sleep(Integer.parseInt(delayInMs));
            GenUtil.println(String.format("To Topic {%s} Sending message : {%s}", topic, new String(e)));
            kafkaTemplate.send(topic, null, e);

        });

    }


    public List<byte[]> getCSVDataAsBytes(final String filePath) throws Exception {
        List<byte[]> returnList = new ArrayList<>();
        final ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> list = GenUtil.readObjectsFromCsv(new File(filePath));
        list.stream().forEach(e -> {
            try {
                returnList.add(mapper.writeValueAsBytes(e));
            } catch (JsonProcessingException ex) {
                ex.printStackTrace();
            }
        });
        return returnList;
    }


}
