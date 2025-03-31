package com.example.blockchain.controller;

import com.example.blockchain.model.Block;
import com.example.blockchain.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "api/blocks")
public class BlockchainController {
    private final BlockchainService blockchainService;

    @Autowired // magically instantiates the blockchainService if it is a component/service (bean)
    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping
    public Map<String, Block> getBlockchain() {
        return blockchainService.getAllBlocks();
    }

    @GetMapping(value = "/{hash}")
    public Block getBlock(@PathVariable("hash") String hash) {
        return blockchainService.getBlock(hash);
    }

    @PostMapping
    public boolean addBlock(@RequestBody Block block) {
        return blockchainService.addBlock(block);
    }

    @DeleteMapping(value = "/{hash}")
    public boolean deleteBlock(@PathVariable("hash") String hash) {
        return blockchainService.deleteBlock(hash);
    }

    @DeleteMapping
    public boolean deleteAllBlocks() {
        return blockchainService.deleteAllBlocks();
    }
}
