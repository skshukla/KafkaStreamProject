package com.sachin.work.kafkastreams;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BaseTest {

    static {
//        System.setProperty("spring.profiles.active", "local");
    }

    @Test
    public void testMe() throws Exception{
        final JSONObject jsonObject = new JSONObject("{\"dept_id\":\"7\",\"dep_name\":\"D-7\",\"dep_rating\":\"3\"}");
        System.out.println("testing..." + jsonObject.getString("dep_rating"));
    }
}
