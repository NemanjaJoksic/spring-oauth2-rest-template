package org.joksin.springoauth2.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan ("org.joksin")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}

