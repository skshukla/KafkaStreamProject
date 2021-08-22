package com.sachin.work.kafkastreams.controller;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class MyController {

    @Autowired
    private Environment environment;

    private ReadOnlyKeyValueStore<String, Long> stateStoreCountStream = null;

    public void setStateStoreForCountStream(KafkaStreams countStream) {
        if (this.stateStoreCountStream != null) {
            return;
        }
        System.out.println("Going to set state store!!");
        this.stateStoreCountStream = countStream.store(
                        StoreQueryParameters.fromNameAndType(
                                this.environment.getProperty("count.stream.store.name"),
                                QueryableStoreTypes.keyValueStore()));
    }

    @GetMapping("me")
    public String me() {
        return "Okay";
    }


    @GetMapping("read-store/{deptId}")
    public String ReadStore(@PathVariable String deptId) {
        if (Objects.isNull(this.stateStoreCountStream)) {
            throw new RuntimeException(String.format("Please ensure the Count Stream profile is set as the count stream profile only triggers this object to be set!!"));
        }
        return String.format("Dept count for deptId {%s} is {%d}", deptId, this.stateStoreCountStream.get(deptId));
    }
}
