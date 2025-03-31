package com.example.blockchain.controller;

import com.example.blockchain.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "api/node")
public class NodeController {
    private final Node node;

    @Autowired
    public NodeController(Node node) {
        this.node = node;
    }

    @GetMapping(value = "/peers")
    public Set<String> getPeers() {
        return node.getPeers();
    }

    @GetMapping(value = "/ping")
    public String ping() {
        return "pong";
    }

}
