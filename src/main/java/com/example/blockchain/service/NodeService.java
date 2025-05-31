package com.example.blockchain.service;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Node;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.response.ApiException;
import com.example.blockchain.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NodeService {
    public static final int MINIMUM_PEERS = 20;
    private final Node node;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
    private final RestTemplate restTemplate = new RestTemplate();
    private final WalletService walletService;

    @Autowired
    public NodeService(Node node, WalletService walletService) {
        this.node = node;
        this.walletService = walletService;
    }

    public ThreadPoolExecutor getExecutor() {
        return executor;
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public Set<String> getPeers() {
        return node.getPeers();
    }

    public PriorityQueue<Transaction> getMemPool() {
        return node.getMemPool();
    }

    public void addTransactionToMemPool(Transaction transaction) {
        if (node.getMemPoolHashes().contains(transaction.getTransactionId())) {
            throw new ApiException("Transaction already exists in mempool", 400);
        }
        node.getMemPool().add(transaction);
        node.getMemPoolHashes().add(transaction.getTransactionId());
    }

    public void removeTransactionFromMemPool(Transaction transaction) {
        node.getMemPool().removeIf((e) -> e.getTransactionId().equals(transaction.getTransactionId()));
        node.getMemPoolHashes().remove(transaction.getTransactionId());
    }

    public boolean memPoolContains(Transaction transaction){
        return node.getMemPoolHashes().contains(transaction.getTransactionId());
    }

    public Block getBlockFromPeers(String hash) {
        Map<Block, Integer> blocks = new HashMap<>();
        for(String peer : node.getPeers()) {
            try {
                String url = peer + "/api/blocks/" + hash;
                Block previousBlock = restTemplate.getForObject(url, Block.class);
                if(!blocks.containsKey(previousBlock)){
                    blocks.put(previousBlock, 1);
                }
                else {
                    blocks.replace(previousBlock, blocks.get(previousBlock) + 1);
                }
            } catch (RestClientException e) {
                System.out.println("[BLOCK SEARCH] No answer from " + peer);
                e.printStackTrace();
                /*if (!isPeerAlive(peer)) {
                    System.out.println("[PEER REMOVAL] " + peer);
                    this.peers.remove(peer);
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Map.Entry<Block, Integer> maxEntry = null;
        for (Map.Entry<Block, Integer> entry : blocks.entrySet()) {
            if (maxEntry == null || entry.getValue() > maxEntry.getValue()) {
                maxEntry = entry;
            }
        }

        return maxEntry != null ? maxEntry.getKey() : null;
    }

    @Scheduled(fixedRate = 10 * 60 * 1000) // auto execute every 10 mins
    private void discoverPeers() {
        for(String peer : node.getPeers()) {
            discoverPeersFrom(peer);
        }
        //System.out.println(this.getPeers());
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    private void removeDeadPeers() {
        for(String peer : node.getPeers()){
            Runnable removeInactivePeers = () -> {
                if (!isPeerAlive(peer)){
                    System.out.println("[DEAD PEER REMOVAL] " + peer);
                    node.getPeers().remove(peer);
                }
            };
            executor.execute(removeInactivePeers);
        }
    }

    public int getAvgMemPoolSize(){
        // Save some statistics locally?
        return 1000;
    }

    public void discoverPeersFrom(String peer){
        Runnable getPeersFromNode = () -> {
            System.out.println("[PEER AUTO DISCOVERY] " + peer);
            String url = peer + "/api/nodes/peers";
            try {
                List<String> peersFromNode = restTemplate.getForObject(url, List.class);
                //System.out.println(peersFromNode);
                if(peersFromNode != null) {
                    node.getPeers().addAll(peersFromNode);
                    if (this.node.getPeers().size() < MINIMUM_PEERS) {
                        for (String newPeer : peersFromNode) {
                            discoverPeersFrom(newPeer);
                        }
                    }
                }
            }
            catch (RestClientException e){
                System.out.println("[NO ANSWER] " + peer);
                if (!isPeerAlive(peer)){
                    System.out.println("[PEER REMOVAL] " + peer);
                    node.getPeers().remove(peer);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
        };
        executor.execute(getPeersFromNode);
    }

    public boolean containsReservedUTXO(Transaction transaction) {
        for(String input : transaction.getInputs()) {
            if(isUTXOReserved(input)) {
                return true;
            }
        }
        return false;
    }

    public boolean sendBlock(Block block){
        CountDownLatch latch = new CountDownLatch(node.getPeers().size());
        AtomicInteger successCount = new AtomicInteger(0);
        for(String peer : node.getPeers()){
            Runnable sendTransactionToPeer = () -> {
                try {
                    String url = peer + "/api/blocks";
                    ApiResponse response = restTemplate.postForObject(url, block, ApiResponse.class);
                    if (response.getStatus() == 200) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    if (!isPeerAlive(peer)) {
                        System.out.println("[BLOCK ERROR] Peer didn't answer " + peer);
                    }
                    else {
                        System.out.println("[BLOCK ERROR] Block wasn't accepted by " + peer);
                    }
                    e.printStackTrace();
                }
                latch.countDown();
            };
            executor.execute(sendTransactionToPeer);
        }

        // Wait for all threads to finish
        try{
            latch.await();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        return successCount.get() >= (node.getPeers().size() + 1) / 2;
    }

    public boolean sendTransaction(Transaction transaction){
        CountDownLatch latch = new CountDownLatch(node.getPeers().size());
        AtomicInteger successCount = new AtomicInteger(0);
        for(String peer : node.getPeers()){
            Runnable sendTransactionToPeer = () -> {
                try {
                    String url = peer + "/api/transactions";
                    ApiResponse response = restTemplate.postForObject(url, transaction, ApiResponse.class);
                    if (response.getStatus() == 200) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    if (!isPeerAlive(peer)) {
                        System.out.println("[TRANSACTION ERROR] Peer didn't answer " + peer);
                    } else {
                        System.out.println("[TRANSACTION ERROR] Transaction wasn't accepted by " + peer);
                    }
                    e.printStackTrace();
                }
                latch.countDown();
            };
            executor.execute(sendTransactionToPeer);
        }

        // Wait for all threads to finish
        try{
            latch.await();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        return successCount.get() >= (node.getPeers().size() + 1) / 2;
    }

    private boolean isPeerAlive(String peer){
        String url = peer + "/api/nodes/ping";
        try{
            ApiResponse response = restTemplate.getForObject(url, ApiResponse.class);
            //System.out.println(response);
            return response != null && response.getStatus() == 200;
        } catch (Exception e){
            //e.printStackTrace();
            return false;
        }
    }

    public Map<String, Block> getUnlinkedBlocks(){
        return node.getUnlinkedBlocks();
    }

    public boolean isUTXOReserved(String key) {
        return node.getReservedUTXOs().contains(key);
    }

    public void reserveUTXO(String key) {
        node.getReservedUTXOs().add(key);
    }

    public void unlockUTXO(String key) {
        node.getReservedUTXOs().remove(key);
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }
}
