package org.example.desktopclient.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import org.example.desktopclient.model.Block;
import org.example.desktopclient.model.BlockchainModel;
import org.example.desktopclient.service.BlockchainService;
import org.example.desktopclient.service.NodeService;
import org.example.desktopclient.service.TransactionService;
import org.example.desktopclient.view.MiningPanel;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BlockchainController {
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static BlockchainController instance;

    private final BlockchainService blockchainService = new BlockchainService();
    private final NodeService nodeService = NodeService.getInstance();
    private final BlockchainModel blockchainModel = BlockchainModel.getInstance();
    private final TransactionService transactionService = TransactionService.getInstance();

    private BlockchainController() {}

    public void startMining(MiningPanel miningPanel, String minerPublicKey, Consumer<String> logCallback) {
        blockchainService.mineBlock(miningPanel, minerPublicKey, logCallback);
    }
    public void stopMining(Consumer<String> logCallback) {
        blockchainService.stopMining(logCallback);
    }

    public void updateBlockchainSizeInBytes() {
        Long blockchainSize = blockchainService.getBlockchainSizeInBytes().join();
        String blockchainSizeFormatted = getFormattedSize(blockchainSize);
        blockchainModel.setBlockchainSizeInBytes(blockchainSizeFormatted);
    }

    public void updateMempoolSizeInBytes() {
        Long mempoolSize = nodeService.getMempoolSizeInBytes().join();
        String mempoolSizeFormatted = getFormattedSize(mempoolSize);
        blockchainModel.setMempoolSizeInBytes(mempoolSizeFormatted);
    }

    public void updateConnectionsCount() {
        blockchainModel.setConnectionsCount(nodeService.getConnectionsCount().join());
    }

    public void updateLastBlockTimestamp() {
        blockchainModel.setLastBlockTimestamp(getTimePassed(blockchainService.getLastBlock().join().getTimeStamp()));
    }

    public void updateFeeAmount() {
        blockchainModel.setFeeAmount(transactionService.getFeeAmount().join());
    }

    public void updateTotalTransactions() {
        blockchainModel.setTotalTransactions(blockchainService.getTotalTransactions().join());
    }

    public SimpleStringProperty getBlockchainSizeInBytesProperty() {
        return blockchainModel.blockchainSizeInBytesProperty();
    }

    public SimpleStringProperty getMempoolSizeInBytesProperty() {
        return blockchainModel.mempoolSizeInBytesProperty();
    }

    public SimpleStringProperty getLastBlockTimestampProperty() {
        return blockchainModel.lastBlockTimestampProperty();
    }

    public SimpleIntegerProperty getFeeAmountProperty() {
        return blockchainModel.feeAmountProperty();
    }

    public SimpleLongProperty getTotalTransactionsProperty() {
        return blockchainModel.totalTransactionsProperty();
    }

    public SimpleIntegerProperty getConnectionsCountProperty() {
        return blockchainModel.connectionsCountProperty();
    }

    public Set<Block> getLastBlocks(){
        return blockchainModel.getLastBlocks();
    }

    public void updateLastBlocks() {
        updateLastBlocks(10);
    }

    public void updateLastBlocks(int count){
        long lastIndex = blockchainModel.getLastBlocks().isEmpty() ? -1 : blockchainModel.getLastBlocks().first().getIndex();
        // Index (str), Block
        Map<Long, Block> lastBlocks = blockchainService.getLastBlocks(lastIndex, count).join();
        blockchainModel.updateLastBlocks(new ArrayList<>(lastBlocks.values()));
    }

    public static BlockchainController getInstance() {
        if (instance == null) {
            instance = new BlockchainController();
        }
        return instance;
    }

    public String getFormattedSize(long sizeInBytes){
        if(sizeInBytes < 11){
            return sizeInBytes + " Bytes";
        }
        float kilo = sizeInBytes / 1024.0F;
        if(kilo < 1024){
            return String.format("%.2f KB", kilo);
        }
        float mega = kilo / 1024.0F;
        if(mega < 1024){
            return String.format("%.2f MB", mega);
        }
        return String.format("%.2f GB", mega / 1024.0F);
    }

    public String getTimePassed(long timeStamp) {
        long sec = (Instant.now().toEpochMilli() - timeStamp) / 1000;
        if(sec < 60){
            return sec + " seconds";
        }
        long mins = sec / 60;
        if(mins < 60){
            return mins + " minutes";
        }
        long hours = mins / 60;
        if(hours < 60){
            return hours + " hours";
        }
        long days = hours / 24;
        if(days < 365){
            return days + " days";
        }
        return days / 365 + " years";
    }
}
