package com.pizzadelivery.server.utils;

import com.pizzadelivery.server.data.entities.Edge;
import com.pizzadelivery.server.services.EdgeService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Navigation {
    List<ArrayList<Edge>> graph;
    Edge[] edges;

    public Navigation(EdgeService edgeService) {
        graph = edgeService.loadGraph().stream().sorted(new Comparator<ArrayList<Edge>>() {
            @Override
            public int compare(ArrayList<Edge> o1, ArrayList<Edge> o2) {
                if (o1.get(0).getId() < o2.get(0).getId())
                    return -1;

                if (o1.get(0).getId() > o2.get(0).getId())
                    return 1;

                return 0;
            }
        }).toList();
        edges = graph.stream().map(list -> list.get(0)).toArray(Edge[]::new);
    }

    public ArrayList<Edge> dijkstraShortestPath(Edge current, Edge target) throws Exception {
        if (graph == null) throw new Exception("Unloaded graph");

        int distance[] = new int[edges.length];
        int parent[] = new int[edges.length];
        for (int j = 0; j < edges.length; j++) {
            distance[j] = Integer.MAX_VALUE;
            parent[j] = 0;
        }

        var node = current.clone();
        int index = node.getId() - 1;
        node.setEdgeWeight(0);

        PriorityQueue<Edge> priorityQueue = new PriorityQueue<Edge>(edges.length, new Edge());
        priorityQueue.add(node);
        distance[index] = 0;

        while (!priorityQueue.isEmpty() && distance[index] < Integer.MAX_VALUE) {
            node = priorityQueue.remove();
            index = node.getId() - 1;

            for (int i = 1; i < graph.get(index).size(); i++) {
                int adjacentIdx = graph.get(index).get(i).getId() - 1;
                Edge adjacent = edges[adjacentIdx];
                int edgeDist = adjacent.getEdgeWeight();
                int newDist = distance[index] + edgeDist;

                // If the new distance is lesser in the cost
                if (newDist < distance[adjacentIdx]) {
                    distance[adjacentIdx] = newDist;
                    parent[adjacentIdx] = index;

                    // Adding the current node to the priority queue priorityQueue
                    adjacent = adjacent.clone();
                    adjacent.setEdgeWeight(distance[adjacentIdx]);
                    priorityQueue.add(adjacent);
                }
            }
        }

        ArrayList<Edge> path = new ArrayList<>();
        navigate(parent, path, current.getId(), target.getId() - 1);

        return path;
    }

    private void navigate(int[] parentNodes, ArrayList<Edge> path, int from, int to) {
        Edge node = edges[parentNodes[to]];
        if (node.getId() == from) {
            return;
        }

        navigate(parentNodes, path, from, node.getId() - 1);
        path.add(node);
    }
}
