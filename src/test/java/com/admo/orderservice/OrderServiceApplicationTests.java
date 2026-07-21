package com.admo.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainRuns() {
        String previous = System.getProperty("server.port");
        System.setProperty("server.port", "0");
        try {
            OrderServiceApplication.main(new String[]{});
        } finally {
            if (previous == null) {
                System.clearProperty("server.port");
            } else {
                System.setProperty("server.port", previous);
            }
        }
    }
}
