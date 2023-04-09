package com.pizzadelivery.server;

import com.pizzadelivery.server.services.EdgeService;
import com.pizzadelivery.server.utils.Navigation;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class ServerApplication {
    @Autowired
    EdgeService edgeService;

    @PostConstruct
    public void init() {
        try {
            new Navigation(edgeService).dijkstraShortestPath(edgeService.findEdge(1),
                    edgeService.findEdge(8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        TimeZone.setDefault(TimeZone.getTimeZone("CET"));
    }

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
