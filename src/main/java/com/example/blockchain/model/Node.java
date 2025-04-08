package com.example.blockchain.model;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class Node {
    private static final Set<String> PUBLIC_NODES = Set.of("http://localhost:8086");
    private String type = "light"; // light / full
    private final Set<String> peers;
    private final PriorityQueue<Transaction> memPool = new PriorityQueue<>();

    public Node() { // String type,
        this.peers = Collections.synchronizedSet(new HashSet<>(PUBLIC_NODES));
        //this.type = type; to be read from conf file
    }

    public Set<String> getPeers() {
        return peers;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public PriorityQueue<Transaction> getMemPool() {
        return memPool;
    }
}
