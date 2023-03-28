package com.pizzadelivery.server.data.entities;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class MapPK implements Serializable {
    @ManyToOne
    @JoinColumn(name = "edge_id", referencedColumnName = "id", nullable = false)
    private Edge edgeByEdgeId;
    @ManyToOne
    @JoinColumn(name = "neighbour_id", referencedColumnName = "id", nullable = false)
    private Edge edgeByNeighbourId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapPK that = (MapPK) o;
        return that.edgeByEdgeId.getId() == edgeByEdgeId.getId()
                && that.edgeByNeighbourId.getId() == edgeByNeighbourId.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(edgeByEdgeId.getId(), edgeByNeighbourId.getId());
    }

    public Edge getEdgeByEdgeId() {
        return edgeByEdgeId;
    }

    public void setEdgeByEdgeId(Edge edgeByEdgeId) {
        this.edgeByEdgeId = edgeByEdgeId;
    }

    public Edge getEdgeByNeighbourId() {
        return edgeByNeighbourId;
    }

    public void setEdgeByNeighbourId(Edge edgeByNeighbourId) {
        this.edgeByNeighbourId = edgeByNeighbourId;
    }
}
