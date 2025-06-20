package org.example.desktopclient.controller;

import org.example.desktopclient.service.NodeService;
import org.example.desktopclient.view.MiningPanel;

public class NodeController {
    private static NodeController instance;
    private final NodeService nodeService = new NodeService();

    private NodeController() {}

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
