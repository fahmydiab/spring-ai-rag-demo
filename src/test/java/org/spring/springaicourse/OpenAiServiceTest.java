package org.spring.springaicourse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
class OpenAiServiceTest {

    @Autowired
    private OpenAiService service;

    @Test
    void generateResponse() {
        String answer = service.generateResponse("Why is the sky blue?");
        assertNotNull(answer);
        assertTrue(answer.contains("scattering"));
    }

}
