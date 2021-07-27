package com.sachin.work.kafkastreams.runners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sachin.work.kafkastreams.util.GenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

@Component
public class PopulateCSVDataToKafkaTopic {


    @Value("${file}")
    private String filePath;

    @Value("${topic}")
    private String topic;

    @Value("${delay:1000}")
    private String delayInMs;

    @Autowired
    @Qualifier("kafkaTemplateByte")
    KafkaTemplate<byte[], byte[]> kafkaTemplateByte;


    @Autowired
    @Qualifier("kafkaTemplateString")
    KafkaTemplate<String, String> kafkaTemplateString;

    @Autowired
    private Environment environment;

//    @Override
    public void run() throws Exception {

        GenUtil.println(String.format("Starting KafkaStream application with filePath {%s}, topic {%s}, delayInMs {%s}", filePath, topic, delayInMs));
        final List<byte[]> dataBytes = getCSVDataAsBytes(filePath);

        IntStream.range(0, dataBytes.size()).forEach(i -> {
            GenUtil.println(String.format("To Topic {%s} Sending message : {%s}", topic, new String(dataBytes.get(i))));
            kafkaTemplateByte.send(topic, UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8), dataBytes.get(i));
//            kafkaTemplateString.send(topic, UUID.randomUUID().toString(), new String(dataBytes.get(i)));
            if (i != dataBytes.size()-1) {
                GenUtil.sleep(Integer.parseInt(delayInMs));
            }
        });
    }

    private List<byte[]> getCSVDataAsBytes(final String filePath) throws Exception {
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
