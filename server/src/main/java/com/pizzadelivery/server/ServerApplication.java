package com.pizzadelivery.server;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ServerApplication {
//    @Autowired
//    Dispatcher dispatcher;

    @PostConstruct
    public void initialize() {
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

//    @PreDestroy
//    public void shutDown() {
//        dispatcher.shutDown();
//    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
