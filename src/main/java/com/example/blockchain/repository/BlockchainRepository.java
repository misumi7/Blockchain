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
        dbDir = new File("/resources/rocks-db", NAME);
        try {
            Files.createDirectories(dbDir.getParentFile().toPath());
            Files.createDirectories(dbDir.getAbsoluteFile().toPath());
            this.db = RocksDB.open(options, dbDir.getAbsolutePath());
        }
        catch (RocksDBException | IOException e) {
            e.printStackTrace();
        }
    }

    public String findBlock(String key){
        String value = null;
        try {
            value = new String(db.get(key.getBytes()));
        }
        catch (RocksDBException e) {
            e.printStackTrace();
        }
        return value;
    }

    public Map<String, String> findAllBlocks(){
        Map<String, String> blockchain = new HashMap<>();
        RocksIterator it = db.newIterator();
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String key = new String(it.key(), StandardCharsets.UTF_8);
            String value = new String(it.value(), StandardCharsets.UTF_8);
            blockchain.put(key, value);
        }
        it.close();
        return blockchain;
    }

    // synchronized as put() and delete() are not thread-safe
    public synchronized boolean saveBlock(Block block) {
        try {
            db.put(block.getBlockHash().getBytes(), block.toJson().getBytes());
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void deleteBlock(String key) {
        try{
            db.delete(key.getBytes());
        }
        catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}
