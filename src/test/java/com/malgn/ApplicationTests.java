package com.malgn;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest(properties = {
    "spring.profiles.active=test",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.sql.init.mode=always"
})
@AutoConfigureMockMvc
class ApplicationTests {

    @Test
    void contextLoads() {
    }

}
