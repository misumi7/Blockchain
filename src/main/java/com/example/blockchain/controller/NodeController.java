package com.example.blockchain.controller;

import com.example.blockchain.model.Node;
import com.example.blockchain.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "api/nodes")
public class NodeController {
    private final NodeService nodeService;

    @Autowired
    public NodeController(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping(value = "/peers")
    public Set<String> getPeers() {
        return nodeService.getPeers();
    }

    @GetMapping(value = "/ping")
    public String ping() {
        return "pong";
    }

}
