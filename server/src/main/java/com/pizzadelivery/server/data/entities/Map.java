package com.pizzadelivery.server.data.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;

import java.io.Serializable;

@Entity
public class Map implements Serializable {
    @EmbeddedId
    private MapPK id;

    public Edge getEdgeByEdgeId() {
        return id.getEdgeByEdgeId();
    }

    public void setEdgeByEdgeId(Edge edgeByEdgeId) {
        id.setEdgeByEdgeId(edgeByEdgeId);
    }

    public Edge getEdgeByNeighbourId() {
        return id.getEdgeByNeighbourId();
    }

    public void setEdgeByNeighbourId(Edge edgeByNeighbourId) {
        id.setEdgeByNeighbourId(edgeByNeighbourId);
    }

    public MapPK getId() {
        return id;
    }

    public void setId(MapPK id) {
        this.id = id;
    }
}
