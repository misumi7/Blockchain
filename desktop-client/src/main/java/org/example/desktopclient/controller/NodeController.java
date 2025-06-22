package org.example.desktopclient.controller;

import com.dlsc.formsfx.view.controls.SimpleBooleanControl;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import org.example.desktopclient.model.NodesModel;
import org.example.desktopclient.service.NodeService;
import org.example.desktopclient.view.MiningPanel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NodeController {
    private static NodeController instance;
    private final NodeService nodeService = NodeService.getInstance();
    private final NodesModel nodesModel = NodesModel.getInstance();

    private NodeController() {}

    public void updateNeighbours() {
        Set<String> peers = nodeService.getNeighbours().join();
        Map<String, Boolean> neighboursStatus = new HashMap<>();
        for(String peer : peers) {
            nodesModel.setNeighbourStatus(peer, nodeService.getPeerStatus(peer));
        }
    }

    public Map<String, SimpleBooleanProperty> getNeighbours() {
        return nodesModel.getNeighbours();
    }

    public void updateMempoolTransactionCount(MiningPanel miningPanel){
        String mempoolSize = nodeService.getMempoolTransactionCount().join();
        miningPanel.setMempoolTransactionCount(mempoolSize);
    }

    public static NodeController getInstance() {
        if (instance == null) {
            instance = new NodeController();
        }
        return instance;
    }
}
