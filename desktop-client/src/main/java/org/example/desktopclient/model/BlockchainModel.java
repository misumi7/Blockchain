package org.example.desktopclient.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.*;

public class BlockchainModel {
    private static BlockchainModel instance;
    SimpleStringProperty blockchainSizeInBytes = new SimpleStringProperty("0 bytes");
    SimpleStringProperty mempoolSizeInBytes = new SimpleStringProperty("0 bytes");
    SimpleIntegerProperty connectionsCount = new SimpleIntegerProperty(0);
    SimpleStringProperty lastBlockTimestamp = new SimpleStringProperty("0 seconds ago");
    SimpleIntegerProperty feeAmount = new SimpleIntegerProperty(0);
    SimpleLongProperty totalTransactions = new SimpleLongProperty(0);
    TreeSet<Block> lastBlocks = new TreeSet<>(Comparator.comparingLong(Block::getIndex));

    private BlockchainModel() {}

    public static BlockchainModel getInstance() {
        if (instance == null) {
            instance = new BlockchainModel();
        }
        return instance;
    }

    public String getBlockchainSizeInBytes() {
        return blockchainSizeInBytes.get();
    }

    public SimpleStringProperty blockchainSizeInBytesProperty() {
        return blockchainSizeInBytes;
    }

    public void setBlockchainSizeInBytes(String blockchainSizeInBytes) {
        this.blockchainSizeInBytes.set(blockchainSizeInBytes);
    }

    public String getMempoolSizeInBytes() {
        return mempoolSizeInBytes.get();
    }

    public SimpleStringProperty mempoolSizeInBytesProperty() {
        return mempoolSizeInBytes;
    }

    public void setMempoolSizeInBytes(String mempoolSizeInBytes) {
        this.mempoolSizeInBytes.set(mempoolSizeInBytes);
    }

    public int getConnectionsCount() {
        return connectionsCount.get();
    }

    public SimpleIntegerProperty connectionsCountProperty() {
        return connectionsCount;
    }

    public void setConnectionsCount(int connectionsCount) {
        this.connectionsCount.set(connectionsCount);
    }

    public String getLastBlockTimestamp() {
        return lastBlockTimestamp.get();
    }

    public SimpleStringProperty lastBlockTimestampProperty() {
        return lastBlockTimestamp;
    }

    public void setLastBlockTimestamp(String lastBlockTimestamp) {
        this.lastBlockTimestamp.set(lastBlockTimestamp);
    }

    public int getFeeAmount() {
        return feeAmount.get();
    }

    public SimpleIntegerProperty feeAmountProperty() {
        return feeAmount;
    }

    public void setFeeAmount(int feeAmount) {
        this.feeAmount.set(feeAmount);
    }

    public long getTotalTransactions() {
        return totalTransactions.get();
    }

    public SimpleLongProperty totalTransactionsProperty() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions.set(totalTransactions);
    }

    public void updateLastBlocks(List<Block> blocksToAdd) {
        lastBlocks.addAll(blocksToAdd);
        System.out.println("Updated last blocks: " + lastBlocks.size() + " blocks");
    }

    public TreeSet<Block> getLastBlocks() {
        return lastBlocks;
    }
}
