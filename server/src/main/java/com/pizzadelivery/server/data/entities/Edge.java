package com.pizzadelivery.server.data.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;

@Entity
public class Edge implements Comparable<Edge>, Comparator<Edge> {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "vertex", nullable = false)
    private int vertex;
    @Basic
    @Column(name = "edge_weight", nullable = false)
    private int edgeWeight;

    @ManyToOne
    @JoinColumn(name = "street_name_id", referencedColumnName = "id", nullable = false)
    private StreetName streetNameByEdgeName;
    @OneToMany(mappedBy = "id.edgeByEdgeId", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SELECT)
    private Collection<Map> mapsById;
    @OneToMany(mappedBy = "id.edgeByNeighbourId")
    private Collection<Map> mapsById_0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVertex() {
        return vertex;
    }

    public void setVertex(int vertex) {
        this.vertex = vertex;
    }

    public int getEdgeWeight() {
        return edgeWeight;
    }

    public void setEdgeWeight(int edgeWeight) {
        this.edgeWeight = edgeWeight;
    }

    //for instances in PrQue
    @Override
    public int compare(Edge node1, Edge node2) {
        return Integer.compare(node1.edgeWeight, node2.edgeWeight);

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

    public Edge clone() {
        var clone = new Edge();
        clone.setId(getId());
        clone.setEdgeWeight(getEdgeWeight());
        clone.setVertex(getVertex());
        clone.setStreetNameByEdgeName(getStreetNameByEdgeName());
        clone.setMapsById(getMapsById());

        return clone;
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

    @Override
    public int compareTo(Edge o) {
        return Integer.compare(getId(), o.getId());
    }
}
