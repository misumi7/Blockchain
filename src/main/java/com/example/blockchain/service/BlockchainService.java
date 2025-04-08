package com.example.blockchain.service;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Blockchain;
import com.example.blockchain.repository.BlockchainRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.*;

@Service
public class BlockchainService {
    private final BlockchainRepository blockchainRepository;
    private final NodeService nodeService;
    private static final int MAX_SEARCH_DEPTH = 100;

    public BlockchainService(BlockchainRepository blockchainRepository, NodeService nodeService) {
        this.blockchainRepository = blockchainRepository;
        this.nodeService = nodeService;
    }

    public Map<String, Block> getAllBlocks() {
        return blockchainRepository.findAllBlocks();
    }

    public Block getBlock(String hash) {
        return blockchainRepository.findBlock(hash);
    }

    public boolean addBlock(Block block) {
        return isValid(block) ? blockchainRepository.saveBlock(block) : false;
    }

    private boolean isValid(Block block){
        if(!block.validateBlock()){
            System.out.println("[BLOCK NOT VALID] Didn't pass proof of work test (" + block.getBlockHash().substring(0, 7) + "..)");
            return false;
        }
        Block previousBlock = blockchainRepository.findBlock(block.getPreviousHash());

        // Previous block wasn't found => can't validate => ask for previous blocks from neighbours
        if(previousBlock == null) {
            Deque<Block> previousBlocks = new ArrayDeque<>();
            Block tempBlock = block;
            for(int depth = 0; depth < MAX_SEARCH_DEPTH; depth++) {
                previousBlock = nodeService.getBlockFromPeers(tempBlock.getPreviousHash());
                if (previousBlock == null) {
                    System.out.println("[TEMP SAVED] Previous block not found (" + block.getBlockHash().substring(0, 7) + "..)");
                    // Save block temporarily (in node)
                    // ...
                    return false;
                } else {
                    previousBlocks.add(previousBlock);
                    tempBlock = previousBlock;
                }
            }
            for(Iterator descItr = previousBlocks.descendingIterator(); descItr.hasNext();) {
                addBlock((Block)descItr.next());
            }
            previousBlock = blockchainRepository.findBlock(block.getPreviousHash());
        }

        if(!Objects.equals(previousBlock.calculateHash(), block.getPreviousHash())){
            System.out.println("[BLOCK NOT VALID] Previous hash didn't match (" + block.getBlockHash().substring(0, 7) + "..)");
            return false;
        }
        return true;
    }

    public boolean deleteBlock(String hash) {
        return blockchainRepository.deleteBlock(hash);
    }

    public boolean deleteAllBlocks() {
        return blockchainRepository.deleteAllBlocks();
    }

}
