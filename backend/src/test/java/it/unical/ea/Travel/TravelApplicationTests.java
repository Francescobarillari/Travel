package it.unical.ea.Travel;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class TravelApplicationTests {

    @MockitoBean
    private JavaMailSender mailSender;
	@Test
	void contextLoads() {
	}

}
