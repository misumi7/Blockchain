package com.example.blockchain.controller;

import com.example.blockchain.model.Block;
import com.example.blockchain.response.ApiResponse;
import com.example.blockchain.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
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

    @GetMapping(value = "/latest")
    public Block getLatestBlock() {
        return blockchainService.getBlock(blockchainService.getLatestBlockIndex());
    }

    @PostMapping
    public ResponseEntity<ApiResponse> addBlock(@RequestBody Block block) {
        if(blockchainService.addBlock(block)){
            return ResponseEntity.ok(new ApiResponse("Block added successfully", 200));
        } else {
            return ResponseEntity.status(400).body(new ApiResponse("Failed to add block", 400));
        }
    }

    @DeleteMapping(value = "/{hash}")
    public boolean deleteBlock(@PathVariable("hash") String hash) {
        return blockchainService.deleteBlock(hash);
    }

    @DeleteMapping
    public boolean deleteAllBlocks() {
        return blockchainService.deleteAllBlocks();
    }

    // TEMP::
    @GetMapping(value = "/mine")
    public void mineBlock() {
        blockchainService.mineBlock();
    }
}
