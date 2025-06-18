package com.example.blockchain.controller;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.response.ApiResponse;
import com.example.blockchain.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping(path = "api/blocks")
public class BlockchainController {
    private final BlockchainService blockchainService;

    @Autowired // instantiates the blockchainService if it is a component/service (bean)
    public BlockchainController(BlockchainService blockchainService) {
        this.blockchainService = blockchainService;
    }

    @GetMapping
    public Map<String, Block> getBlockchain() {
        return blockchainService.getAllBlocks();
    }

    @GetMapping(value = "/size")
    public long getBlockchainSizeInBytes() {
        //System.out.println("Getting blockchain size in bytes || " + blockchainService.getBlockchainSizeInBytes());
        return blockchainService.getBlockchainSizeInBytes();
    }

    @GetMapping(value = "/sync")
    public ResponseEntity<ApiResponse> isBlockchainSync() {
        boolean isSync = blockchainService.isBlockchainSync();
        if (isSync) {
            return ResponseEntity.ok(new ApiResponse("Blockchain is synchronized", 200));
        } else {
            return ResponseEntity.status(400).body(new ApiResponse("Blockchain is not synchronized", 400));
        }
    }

    @GetMapping(value = "/transaction-count")
    public long getTransactionCount() {
        return blockchainService.getTransactionCount();
    }

    @GetMapping(params = {"from", "count"})
    public Map<Long, Block> getBlocks(@RequestParam("from") long from, @RequestParam("count") long count) {
        return blockchainService.getBlocks(from, count);
    }

    @GetMapping(value = "/{hash}")
    public Block getBlock(@PathVariable("hash") String hash) {
        return blockchainService.getBlock(hash);
    }

    @GetMapping(value = "/mining-reward")
    public long getMiningReward() {
        return blockchainService.getCurrentMiningReward();
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

    @GetMapping(value = "/mining/block-to-mine")
    public Block getBlockToMine(@RequestParam(value = "minerPublicKey") String minerPublicKey) {
        return blockchainService.getBlockToMine(minerPublicKey);
    }

    @GetMapping(value = "/mining/logs", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMiningLogs() {
        SseEmitter emitter = new SseEmitter(0L);
        blockchainService.registerEmitter(emitter);

        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data("connected"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return emitter;
    }

    /*@GetMapping(value = "/mine")
    public ResponseEntity<ApiResponse> mineBlock(@RequestParam("minerPublicKey") String minerPublicKey) {
        blockchainService.mineBlock(minerPublicKey);
        return ResponseEntity.ok(new ApiResponse("Mining started", 200));
    }*/
}
