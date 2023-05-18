package com.pizzadelivery.server.services;

import com.pizzadelivery.server.data.entities.Edge;
import com.pizzadelivery.server.data.repositories.EdgeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EdgeService {
    private final EdgeRepository edgeRepository;

    @Autowired
    public EdgeService(EdgeRepository edgeRepository) {
        this.edgeRepository = edgeRepository;
    }

    public Edge findEdge(int id) {
        return edgeRepository.findById(id).orElseThrow();
    }

    public Iterable<Edge> loadEdges() {
        Iterable<Edge> all = edgeRepository.findAll();
        loadGraph();
        return all;
    }

    public List<ArrayList<Edge>> loadGraph() {
        Iterable<Edge> all = edgeRepository.findAll();
        ArrayList<ArrayList<Edge>> graph = new ArrayList<>();
        all.forEach(edge -> {
            var adjacency = new ArrayList<>(List.of(edge));
            edge.getMapsById().forEach(id -> adjacency.add(id.getEdgeByNeighbourId()));
            graph.add(adjacency);
        });

        return graph.stream().sorted(Comparator.comparing(o -> o.get(0))).toList();
    }
}
