package com.pizzadelivery.server.utils;

import com.pizzadelivery.server.data.entities.Car;
import com.pizzadelivery.server.data.entities.Edge;
import com.pizzadelivery.server.services.EdgeService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
class NavigationTest {
    @Autowired
    EdgeService edgeService;
    Navigation navigation;

    @BeforeEach
    void setUp() {
        navigation = new Navigation(edgeService);
        // mini section of the map
        mockGraph(6);
    }


    @Test
    void navigate() {
        Car mockCar = new Car();
        Edge start = edgeService.findEdge(1);
        Edge[] oneStep = navigation.dijkstraShortestPath(start, start);
        Assertions.assertEquals(1, oneStep.length);
        Assertions.assertEquals(start, oneStep[0]);

        Edge finish = edgeService.findEdge(2);
        Edge[] twoStep = navigation.dijkstraShortestPath(start, finish);
        Assertions.assertEquals(2, twoStep.length);
        Assertions.assertEquals(start, twoStep[0]);
        Assertions.assertEquals(finish, twoStep[1]);

        Edge bartok = edgeService.findEdge(5);
        Edge[] threeStep = navigation.dijkstraShortestPath(start, bartok);
        Assertions.assertEquals(3, threeStep.length);

        Edge bocskai = edgeService.findEdge(3);
        Edge villanyi = edgeService.findEdge(4);
        navigation.navigate(mockCar, bocskai, villanyi);
        Assertions.assertEquals(4, navigation.getRoute(mockCar).length);
        Assertions.assertEquals(5500, navigation.getDistance(mockCar));

        //extending the graph only by one edge, enables dijkstra to find shorter path than previous
        mockGraph(7);
        navigation.navigate(mockCar, bocskai, villanyi);
        Assertions.assertEquals(4, navigation.getRoute(mockCar).length);
        Assertions.assertEquals(4560, navigation.getDistance(mockCar));
    }

    private void mockGraph(int until) {
        List<Integer> mockIds = IntStream.range(1, until + 1).boxed().toList();
        var mockGraph = new ArrayList<>(edgeService.loadGraph().stream().filter(arr -> mockIds
                .contains(arr.get(0).getId())).toList());
        mockGraph.replaceAll(edges -> new ArrayList<>(edges.stream().filter(edge -> mockIds.contains(edge.getId())).toList()));
        navigation.setGraph(mockGraph);
    }
}