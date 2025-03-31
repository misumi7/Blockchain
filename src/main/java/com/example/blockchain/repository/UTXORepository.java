package com.example.blockchain.repository;


import com.example.blockchain.model.Block;
import com.example.blockchain.model.UTXO;
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
public class UTXORepository {
    private final static String NAME = "utxo-db";
    File dbDir;
    RocksDB db;

    public UTXORepository() {
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

    public boolean saveUTXO(UTXO utxo){
        try {
            db.put((utxo.getTxId() + "-" + utxo.getOutputIndex()).getBytes(), UTXO.toJson(utxo).getBytes());
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, UTXO> getAllUTXOs(){
        Map<String, UTXO> utxoList = new HashMap<>();
        RocksIterator it = db.newIterator();
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String key = new String(it.key(), StandardCharsets.UTF_8);
            UTXO value = UTXO.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            utxoList.put(key, value);
        }
        it.close();
        return utxoList;
    }

    public UTXO getUTXO(String txId, int outputIndex) {
        UTXO value = null;
        try {
            value = UTXO.fromJson(new String(db.get((txId + "-" + outputIndex).getBytes())));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return value;
    }

    public Map<String, UTXO> findUtxoByOwner(String publicKey) {
        Map<String, UTXO> pKeyUtxoList = new HashMap<>();
        RocksIterator it = db.newIterator();
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String key = new String(it.key(), StandardCharsets.UTF_8);
            UTXO value = UTXO.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            if (value != null && value.getOwner().equals(publicKey)) {
                pKeyUtxoList.put(key, value);
            }
        }
        it.close();
        return pKeyUtxoList;
    }
}
