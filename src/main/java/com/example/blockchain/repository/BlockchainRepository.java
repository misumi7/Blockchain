package com.example.blockchain.repository;

import com.example.blockchain.model.Block;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Repository
public class BlockchainRepository {
    private final static String NAME = "blockchain-db";
    File dbDir;
    RocksDB db;

    public BlockchainRepository() {
        RocksDB.loadLibrary();
        final Options options = new Options();
        options.setCreateIfMissing(true);
        dbDir = new File(System.getProperty("user.dir") + "src/main/java/com/example/blockchain/data/", NAME);
        try {
            Files.createDirectories(dbDir.getParentFile().toPath());
            Files.createDirectories(dbDir.getAbsoluteFile().toPath());
            this.db = RocksDB.open(options, dbDir.getAbsolutePath());
        }
        catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }
    }

    public Block findBlock(String key){
        try {
            byte[] blockData = db.get(key.getBytes());
            return blockData != null ? Block.fromJson(new String(blockData)) : null;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Block> findAllBlocks(){
        Map<String, Block> blockchain = new HashMap<>();
        RocksIterator it = db.newIterator();
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
        try {
            db.put(block.getBlockHash().getBytes(), Block.toJson(block).getBytes());
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean deleteBlock(String key) {
        try{
            db.delete(key.getBytes());
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }


    public synchronized boolean deleteAllBlocks(){
        RocksIterator it = db.newIterator();
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String key = new String(it.key(), StandardCharsets.UTF_8);
            try {
                db.delete(key.getBytes());
            }
            catch (RocksDBException e) {
                e.printStackTrace();
                return false;
            }
        }
        it.close();
        return true;
    }

}
