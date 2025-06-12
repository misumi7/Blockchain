package com.example.blockchain.model;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.ConnectException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class Node {
    public static final Set<String> PUBLIC_NODES = Set.of("http://localhost:8086");
    private String type = "light"; // light / full
    private final Set<String> peers;

    // don't forget to reserve input utxos of transactions that get in the mempool
    private final PriorityQueue<Transaction> memPool = new PriorityQueue<>(Comparator.comparing(Transaction::getTransactionFee).reversed());
    private final Set<String> memPoolHashes = ConcurrentHashMap.newKeySet();
    private final Map<String, Block> unlinkedBlocks = new HashMap<>();

    // test when create and validate transactions if their inputs are not reserved
    // if they are, then transaction is invalid
    private final Set<String> reservedUTXOs = new HashSet<>();

    public Node() {
        // String type,
        this.peers = ConcurrentHashMap.newKeySet();
        this.peers.addAll(PUBLIC_NODES);
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

    public Set<String> getReservedUTXOs() {
        return reservedUTXOs;
    }

    public Map<String, Block> getUnlinkedBlocks() {
        return unlinkedBlocks;
    }

    public Set<String> getMemPoolHashes() {
        return memPoolHashes;
    }
}
