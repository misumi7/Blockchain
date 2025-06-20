package org.example.desktopclient.controller;

import org.example.desktopclient.service.BlockchainService;
import org.example.desktopclient.view.MiningPanel;

import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class BlockchainController {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static BlockchainController instance;

    private final BlockchainService blockchainService = new BlockchainService();

    private BlockchainController() {}

    public void startMining(MiningPanel miningPanel, String minerPublicKey, Consumer<String> logCallback) {
        blockchainService.mineBlock(miningPanel, minerPublicKey, logCallback);
    }
    public void stopMining(Consumer<String> logCallback) {
        blockchainService.stopMining(logCallback);
    }

    public static BlockchainController getInstance() {
        if (instance == null) {
            instance = new BlockchainController();
        }
        return instance;
    }

}
