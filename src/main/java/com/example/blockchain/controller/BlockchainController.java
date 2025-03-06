package com.example.blockchain.controller;

import com.example.blockchain.model.Block;
import com.example.blockchain.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "api/blockchain")
public class BlockchainController {
    private final BlockchainService blockchainService;

    @Autowired // magically instantiates the blockchainService if it is a component/service (bean)
    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping(value = "/blocks")
    public Map<String, String> getBlockchain() {
        return blockchainService.getAllBlocks();
    }

    @GetMapping(value = "/blocks/{hash}")
    public String getBlock(@PathVariable("hash") String hash) {
        return blockchainService.getBlock(hash);
    }

    @PostMapping(value = "/blocks")
    public boolean addBlock(@RequestBody Block block) {
        return blockchainService.addBlock(block);
    }
}
