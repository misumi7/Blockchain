package com.example.blockchain.repository;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import org.rocksdb.*;
import org.springframework.stereotype.Repository;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BlockchainRepository {
    private RocksDB db;
    private ColumnFamilyHandle blockCF;
    private ColumnFamilyHandle blockIndexCF;

    public BlockchainRepository(BaseRepository baseRepository) {
        this.db = baseRepository.getDb();
        this.blockCF = baseRepository.getBlockCF();
        this.blockIndexCF = baseRepository.getBlockIndexCF();
    }

    public synchronized List<Transaction> getWalletTransactions(String walletPublicKey){
        List<Transaction> transactions = new ArrayList<>();
        RocksIterator it = db.newIterator(blockCF);
        for(it.seekToFirst(); it.isValid(); it.next()) {
            Block block = Block.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            assert block != null;
            for(Transaction transaction : block.getTransactions()) {
                //System.out.println(transaction);
                if (transaction != null &&
                        (transaction.getSenderPublicKey() != null && transaction.getSenderPublicKey().equals(walletPublicKey)) ||
                        (transaction.getReceiverPublicKey() != null && transaction.getReceiverPublicKey().equals(walletPublicKey))) {
                    transactions.add(transaction);
                }
            }
        }
        it.close();
        return transactions;
    }

    public synchronized Block getBlock(String key){
        try {
            byte[] blockData = db.get(blockCF, key.getBytes());
            return blockData != null ? Block.fromJson(new String(blockData)) : null;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Block getBlock(long index){
        try {
            byte[] key = ByteBuffer.allocate(8).putLong(index).array();
            byte[] blockHash = db.get(blockIndexCF, key);
            if(blockHash != null) {
                return getBlock(new String(blockHash, StandardCharsets.UTF_8));
            }
            else {
                return null;
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized Map<String, Block> getAllBlocks(){
        Map<String, Block> blockchain = new HashMap<>();
        RocksIterator it = db.newIterator(blockCF);
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String key = new String(it.key(), StandardCharsets.UTF_8);
            Block value = Block.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            blockchain.put(key, value);
        }
        it.close();
        return blockchain;
    }

    // synchronized as put() and delete() are not thread-safe
    public synchronized boolean saveBlock(Block block) {
        try(WriteBatch batch = new WriteBatch()) {
            batch.put(blockCF, block.getBlockHash().getBytes(), Block.toJson(block).getBytes());
            batch.put(blockIndexCF, ByteBuffer.allocate(8).putLong(block.getIndex()).array(), block.getBlockHash().getBytes());
            db.write(new WriteOptions(), batch);
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean deleteBlock(String key) {
        try(WriteBatch batch = new WriteBatch()) {
            byte[] index = ByteBuffer.allocate(8).putLong(getBlock(key).getIndex()).array();
            batch.delete(blockIndexCF, index);
            batch.delete(blockCF, key.getBytes());
            db.write(new WriteOptions(), batch);
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean deleteAllBlocks(){
        try(WriteBatch batch = new WriteBatch()) {
            RocksIterator blockIt = db.newIterator(blockCF);
            RocksIterator blockIndexIt = db.newIterator(blockIndexCF);
            for (blockIt.seekToFirst(); blockIt.isValid(); blockIt.next()) {
                String key = new String(blockIt.key(), StandardCharsets.UTF_8);
                batch.delete(blockCF, key.getBytes());
            }
            for (blockIndexIt.seekToFirst(); blockIndexIt.isValid(); blockIndexIt.next()) {
                batch.delete(blockIndexCF, blockIndexIt.key());
            }
            blockIt.close();
            blockIndexIt.close();
            db.write(new WriteOptions(), batch);
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public long getLatestBlockIndex() {
        try(RocksIterator it = db.newIterator(blockIndexCF)) {
            it.seekToLast();
            return it.isValid() ? ByteBuffer.wrap(it.key()).getLong() : 0;
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public String getLatestBlockHash() {
        try(RocksIterator it = db.newIterator(blockIndexCF)) {
            it.seekToLast();
            return it.isValid() ? new String(it.value(), StandardCharsets.UTF_8) : null;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
