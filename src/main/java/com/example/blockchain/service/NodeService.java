package com.example.blockchain.service;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class NodeService {
    private final Node node;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(15);
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired // this constructor will be used by spring
    public NodeService(Node node) {
        this.node = node;
        discoverPeers();
    }

    public Set<String> getPeers() {
        return node.getPeers();
    }

    public Block getBlockFromPeers(String hash) {
        // To do: test this method and re-test discover and remove dead peers
        Map<Block, Integer> blocks = new HashMap<>();
        for(String peer : node.getPeers()) {
            try {
                //System.out.println("[PREV. BLOCK NOT FOUND] " + hash.substring(0, 7) + "..");
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
                // ?? should we
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
            Runnable getPeersFromNode = () -> {
                System.out.println("[PEER AUTO DISCOVERY] " + peer);
                String url = peer + "/api/node/peers";
                try {
                    List<String> peersFromNode = restTemplate.getForObject(url, List.class);
                    node.getPeers().addAll(peersFromNode);
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
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    private void removeDeadPeers() {
        for(String peer : node.getPeers()){
            Runnable removeInactivePeers = () -> {
                if (!isPeerAlive(peer)){
                    System.out.println("[PEER REMOVAL] " + peer);
                    node.getPeers().remove(peer);
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
}
