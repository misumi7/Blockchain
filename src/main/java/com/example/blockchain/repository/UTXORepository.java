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
import java.util.*;

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
            db.put((utxo.getOwner() + ":" + utxo.getTxId() + ":" + utxo.getOutputIndex()).getBytes(), UTXO.toJson(utxo).getBytes());
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

    public UTXO getUTXO(String ownerPublicKey, String txId, int outputIndex) {
        UTXO value = null;
        try {
            value = UTXO.fromJson(new String(db.get((ownerPublicKey + ":" + txId + ":" + outputIndex).getBytes())));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return value;
    }

    public List<UTXO> getUtxoByOwner(String publicKey) {
        return getUtxoByOwner(publicKey, false);
    }

    public List<UTXO> getUtxoByOwner(String publicKey, boolean sort) {
        List<UTXO> utxoList = new ArrayList<>();
        RocksIterator it = db.newIterator();
        for(it.seek(publicKey.getBytes()); it.isValid() && (new String(it.key(), StandardCharsets.UTF_8).startsWith(publicKey)); it.next()) {
            //String key = new String(it.key(), StandardCharsets.UTF_8);
            UTXO value = UTXO.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            if (value != null && value.getOwner().equals(publicKey)) {
                utxoList.add(value);
            }
        }
        it.close();
        if(sort){
            utxoList.sort(Comparator.comparingDouble(UTXO::getAmount));
        }
        return utxoList;
    }

    public boolean deleteUTXO(String publicKey, String txId, int outputIndex) {
        try {
            db.delete((publicKey + ":" + txId + ":" + outputIndex).getBytes());
            return true;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }
}
