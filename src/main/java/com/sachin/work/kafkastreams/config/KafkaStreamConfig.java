package com.sachin.work.kafkastreams.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaStreamConfig {

    @Value("${kafka.brokers}")
    private String KAFKA_BROKERS;

    @Bean
    public KafkaTemplate<byte[], byte[]> kafkaTemplate_ByteArray() {
        final KafkaTemplate<byte[], byte[]> kafkaTemplate = new KafkaTemplate<>(this.producerFactory_ByteArray());
        return kafkaTemplate;
    }

    private <T> ProducerFactory<byte[], byte[]> producerFactory_ByteArray() {
        final Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_BROKERS);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }
}
