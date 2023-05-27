package com.pizzadelivery.server;

import com.pizzadelivery.server.utils.Dispatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@SpringBootApplication
@EnableWebSocketMessageBroker
@EnableWebSocket
public class ServerApplication {
    @Autowired
    Dispatcher dispatcher;

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
