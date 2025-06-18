package org.example.thesisdesktop.controller;

import org.example.thesisdesktop.service.NodeService;
import org.example.thesisdesktop.view.Node;

import java.util.concurrent.CompletableFuture;

public class NodeController {
    private static NodeController instance;
    private final NodeService nodeService = new NodeService();

    private NodeController() {}

    public void updateMempoolTransactionCount(Node node){
        String mempoolSize = nodeService.getMempoolTransactionCount().join();
        node.setMempoolTransactionCount(mempoolSize);
    }

    public static NodeController getInstance() {
        if (instance == null) {
            instance = new NodeController();
        }
        return instance;
    }
}
