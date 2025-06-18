package org.example.thesisdesktop.controller;

import org.example.thesisdesktop.model.Block;
import org.example.thesisdesktop.service.BlockchainService;
import org.example.thesisdesktop.service.NodeService;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class BlockchainController {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static BlockchainController instance;

    private final BlockchainService blockchainService = new BlockchainService();

    private BlockchainController() {}

    public void startMining(String minerPublicKey, Consumer<String> logCallback) {
        blockchainService.mineBlock(minerPublicKey, logCallback);
    }

    public static BlockchainController getInstance() {
        if (instance == null) {
            instance = new BlockchainController();
        }
        return instance;
    }
}
