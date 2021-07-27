package com.sachin.work.kafkastreams;

import com.sachin.work.kafkastreams.runners.CountStreamExample;
import com.sachin.work.kafkastreams.runners.PopulateCSVDataToKafkaTopic;
import com.sachin.work.kafkastreams.util.GenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;


import java.util.Arrays;

/**
 * To execute in local environment
 * mvn clean spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
 */

@SpringBootApplication
public class KafkaStreamApplication {
    static {
        // Un-Comment to run for local development
//        System.setProperty("spring.profiles.active", "local");
//        System.setProperty("runner", "count-stream");

    }

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext context;

    public static void main(String[] args) throws Exception {
        ApplicationContext context = SpringApplication.run(KafkaStreamApplication.class, args);
        context.getBean(KafkaStreamApplication.class).run();
        GenUtil.sleep(1000);
    }

    private void run() throws Exception {
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
                context.getBean(CountStreamExample.class).run();
                break;
            default:
                context.getBean(PopulateCSVDataToKafkaTopic.class).run();
        }
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



