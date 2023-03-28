package com.pizzadelivery.server.data.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

import java.util.Collection;
import java.util.Objects;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = Edge.class)
@Entity
public class Edge {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "vertex", nullable = false)
    private byte vertex;
    @Basic
    @Column(name = "edge_weight", nullable = false)
    private int edgeWeight;

    @ManyToOne
    @JoinColumn(name = "edge_name", referencedColumnName = "id", nullable = false)
    private StreetName streetNameByEdgeName;
    @OneToMany(mappedBy = "id.edgeByEdgeId")
    private Collection<Map> mapsById;
    @OneToMany(mappedBy = "id.edgeByNeighbourId")
    private Collection<Map> mapsById_0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getVertex() {
        return vertex;
    }

    public void setVertex(byte vertex) {
        this.vertex = vertex;
    }

    public int getEdgeWeight() {
        return edgeWeight;
    }

    public void setEdgeWeight(int edgeWeight) {
        this.edgeWeight = edgeWeight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return id == edge.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public StreetName getStreetNameByEdgeName() {
        return streetNameByEdgeName;
    }

    public void setStreetNameByEdgeName(StreetName streetNameByEdgeName) {
        this.streetNameByEdgeName = streetNameByEdgeName;
    }

    public Collection<Map> getMapsById() {
        return mapsById;
    }

    public void setMapsById(Collection<Map> mapsById) {
        this.mapsById = mapsById;
    }

    public Collection<Map> getMapsById_0() {
        return mapsById_0;
    }

    public void setMapsById_0(Collection<Map> mapsById_0) {
        this.mapsById_0 = mapsById_0;
    }
}
