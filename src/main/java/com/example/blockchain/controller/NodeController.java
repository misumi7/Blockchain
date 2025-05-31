package com.example.blockchain.controller;

import com.example.blockchain.model.Node;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.response.ApiResponse;
import com.example.blockchain.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.PriorityQueue;
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

    @GetMapping(value = "/mempool")
    public PriorityQueue<Transaction> getMemPool() {
        return nodeService.getMemPool();
    }

    @GetMapping(value = "/ping")
    public ResponseEntity<ApiResponse> ping() {
        return ResponseEntity.ok(new ApiResponse("Peer is alive", 200));
    }

}
