package com.sachin.work.kafkastreams;

import com.sachin.work.kafkastreams.runners.CountStreamExample;
import com.sachin.work.kafkastreams.runners.PopulateCSVDataToKafkaTopic;
import com.sachin.work.kafkastreams.util.GenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;


import java.util.Arrays;
import java.util.Collections;

/**
 * To execute in local environment
 * mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
 */

@SpringBootApplication
public class KafkaStreamApplication {
    static {
        // Un-Comment to run for local development
        System.setProperty("spring.profiles.active", "local");
        System.setProperty("runner", "count-stream");
    }

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(KafkaStreamApplication.class, args);
//        ApplicationContext context = SpringApplication.run(KafkaStreamApplication.class, args);
//        Thread.currentThread().sleep(10 * 60 * 1000);
    }

    @Component
    class Starter {

        @EventListener(ApplicationReadyEvent.class)
        public void start() throws Exception {
            System.out.println("I am started...!!!!");
            run();
        }
    }

    private void run() throws Exception {
        printAllEnvValues();
        GenUtil.println(String.format("Runner string val {%s}", this.environment.getProperty("runner")));
        final RUNNER_PROGS runner = StringUtils.isEmpty(this.environment.getProperty("runner")) ? RUNNER_PROGS.POPULATE_CSV_DATA
                : Arrays.stream(RUNNER_PROGS.values())
                .filter(e -> e.getKey().equals(this.environment.getProperty("runner"))).findFirst().get();
        GenUtil.println(String.format("Runner enum val {%s}", runner));
        switch (runner) {
            case POPULATE_CSV_DATA:
                context.getBean(PopulateCSVDataToKafkaTopic.class).run();
                break;
            case COUNT_STREAM:
                context.getBean(CountStreamExample.class).run_Employee();
                break;
            default:
                context.getBean(PopulateCSVDataToKafkaTopic.class).run();
        }
    }

    private static void printAllEnvValues() {
        GenUtil.println("\nGoing to print Environment values:\n---------------------->\n");
        System.getenv().entrySet().stream().sorted( (o1, o2) -> o1.getKey().compareTo(o2.getKey())).forEach(e -> {
            GenUtil.println(String.format("Env key {%s} has value {%s}", e.getKey(), e.getValue()));
        });
        GenUtil.println("----------------------\n");
    }

    static enum RUNNER_PROGS {
        POPULATE_CSV_DATA("populate-csv-data"),
        COUNT_STREAM("count-stream");

        RUNNER_PROGS(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }

        private String key;
    }
}



