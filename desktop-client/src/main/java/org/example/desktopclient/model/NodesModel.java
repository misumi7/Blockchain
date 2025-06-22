package org.example.desktopclient.model;

import javafx.beans.property.SimpleBooleanProperty;

import java.util.HashMap;
import java.util.Map;

public class NodesModel {
    private static NodesModel instance;
    Map<String, SimpleBooleanProperty> neighbours = new HashMap<>();

    private NodesModel() {}

    public static NodesModel getInstance() {
        if (instance == null) {
            instance = new NodesModel();
        }
        return instance;
    }

    public void setNeighbours(Map<String, SimpleBooleanProperty> neighboursStatus) {
        this.neighbours = neighboursStatus;
    }

    public void setNeighbourStatus(String neighbour, boolean status) {
        if (neighbours.containsKey(neighbour)) {
            neighbours.get(neighbour).set(status);
        } else {
            neighbours.put(neighbour, new SimpleBooleanProperty(status));
        }
    }

    public Map<String, SimpleBooleanProperty> getNeighbours() {
        return neighbours;
    }
}
