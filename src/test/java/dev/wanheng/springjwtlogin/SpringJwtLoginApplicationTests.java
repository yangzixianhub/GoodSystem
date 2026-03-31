package dev.wanheng.springjwtlogin;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
        partitions = 1,
        topics = "seckill-order-create",
        brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
class SpringJwtLoginApplicationTests {

    @Test
    void contextLoads() {
    }

}
