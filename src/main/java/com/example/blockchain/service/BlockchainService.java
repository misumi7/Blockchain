package com.example.blockchain.service;

import com.example.blockchain.model.Block;
import com.example.blockchain.repository.BlockchainRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BlockchainService {
    private final BlockchainRepository blockchainRepository;

    public BlockchainService(BlockchainRepository blockchainRepository) {
        this.blockchainRepository = blockchainRepository;
    }

    public Map<String, Block> getAllBlocks() {
        return blockchainRepository.findAllBlocks();
    }

    public Block getBlock(String hash) {
        return blockchainRepository.findBlock(hash);
    }

    public boolean addBlock(Block block) {
        return blockchainRepository.saveBlock(block);
    }

    public boolean deleteBlock(String hash) {
        return blockchainRepository.deleteBlock(hash);
    }

    public boolean deleteAllBlocks() {
        return blockchainRepository.deleteAllBlocks();
    }

}
