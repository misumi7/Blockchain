package com.example.blockchain.controller;

import com.example.blockchain.model.Node;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.response.ApiResponse;
import com.example.blockchain.service.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

@CrossOrigin(origins = "http://localhost:5173")
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

    @GetMapping(value = "/peers/count")
    public int getPeersCount() {
        return nodeService.getPeersCount();
    }

    @GetMapping(value = "/mempool")
    public PriorityQueue<Transaction> getMemPool() {
        return nodeService.getMemPool();
    }

    @GetMapping(value = "/mempool/count")
    public int getMemPoolCount() {
        return nodeService.getMemPoolSize();
    }

    @GetMapping(value = "/mempool/size")
    public int getMemPoolSizeInBytes() {
        return nodeService.getMemPoolSizeInBytes();
    }

    @GetMapping(value = "/ping", params = {"addr"})
    public ResponseEntity<ApiResponse> ping(@RequestParam("addr") String ipAddress) {
        nodeService.ping(ipAddress);
        return ResponseEntity.ok(new ApiResponse("Peer " + ipAddress + " is alive", 200));
    }


    @GetMapping(value = "/ping")
    public ResponseEntity<ApiResponse> ping() {
        return ResponseEntity.ok(new ApiResponse("Peer is alive", 200));
    }

}
