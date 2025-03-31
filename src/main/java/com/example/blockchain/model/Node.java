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
    //private static final int MAX_NEIGHBOURS = 50;
    private String type = "light"; // light / full
    private Set<String> peers;
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
    private RestTemplate restTemplate = new RestTemplate();

    /*
     Connect to public nodes and get the current version of the blockchain.
     If the node type is full, it can send the new mined block to his neighbours and receive new blocks
     from them and re-send them to other neighbours.
    */

    public Node() { // String type,
        this.peers = Collections.synchronizedSet(new HashSet<>(PUBLIC_NODES));
        //this.type = type; to be read from conf file
        discoverPeers();
        // to add public nodes connection test
    }


    @Scheduled(fixedRate = 10 * 60 * 1000) // auto execute every 10 mins
    private void discoverPeers() {
        System.out.println("discoverPeers discoverPeers discoverPeers discoverPeers\n");
        for(String peer : this.peers) {
            Runnable getPeersFromNode = () -> {
                System.out.println("[PEER AUTO DISCOVERY] " + peer);
                String url = peer + "/api/node/peers";
                try {
                    List<String> peersFromNode = restTemplate.getForObject(url, List.class);
                    this.peers.addAll(peersFromNode);
                }
                catch (RestClientException e){
                    System.out.println("[NO ANSWER] " + peer);
                    if (!isPeerAlive(peer)){
                        System.out.println("[PEER REMOVAL] " + peer);
                        this.peers.remove(peer);
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            };
            executor.execute(getPeersFromNode);
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    private void removeDeadPeers() {
        for(String peer : this.peers){
            Runnable removeInactivePeers = () -> {
                if (!isPeerAlive(peer)){
                    System.out.println("[PEER REMOVAL] " + peer);
                    this.peers.remove(peer);
                }
            };
            executor.execute(removeInactivePeers);
        }
    }

    private boolean isPeerAlive(String peer){
        String url = peer + "/api/node/ping";
        try{
            String response = restTemplate.getForObject(url, String.class);
            return response.equals("pong");
        } catch (Exception e){
            return false;
        }
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

}
