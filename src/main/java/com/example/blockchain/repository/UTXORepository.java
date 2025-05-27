package com.example.blockchain.repository;


import com.example.blockchain.model.Block;
import com.example.blockchain.model.UTXO;
import org.rocksdb.*;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Repository
public class UTXORepository{
    private RocksDB db;
    private ColumnFamilyHandle utxoCF;

    public UTXORepository(BaseRepository baseRepository) {
        this.db = baseRepository.getDb();
        this.utxoCF = baseRepository.getUtxoCF();
    }

    public synchronized boolean saveUTXO(UTXO utxo){
        try {
            db.put(utxoCF, (utxo.getOwner() + ":" + utxo.getTxId() + ":" + utxo.getOutputIndex()).getBytes(), UTXO.toJson(utxo).getBytes());
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized Map<String, UTXO> getAllUTXOs(){
        Map<String, UTXO> utxoList = new HashMap<>();
        RocksIterator it = db.newIterator(utxoCF);
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String key = new String(it.key(), StandardCharsets.UTF_8);
            UTXO value = UTXO.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            utxoList.put(key, value);
        }
        it.close();
        return utxoList;
    }

    public synchronized UTXO getUTXO(String ownerPublicKey, String txId, int outputIndex) {
        UTXO value = null;
        try {
            value = UTXO.fromJson(new String(db.get(utxoCF, (ownerPublicKey + ":" + txId + ":" + outputIndex).getBytes())));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return value;
    }

    public synchronized List<UTXO> getUtxoByOwner(String publicKey) {
        return getUtxoByOwner(publicKey, false);
    }

    public synchronized List<UTXO> getUtxoByOwner(String publicKey, boolean sort) {
        List<UTXO> utxoList = new ArrayList<>();
        RocksIterator it = db.newIterator(utxoCF);
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

    public synchronized boolean deleteUTXO(String publicKey, String txId, int outputIndex) {
        try {
            db.delete(utxoCF, (publicKey + ":" + txId + ":" + outputIndex).getBytes());
            return true;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized void deleteAllUTXO(){
        RocksIterator it = db.newIterator(utxoCF);
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String key = new String(it.key(), StandardCharsets.UTF_8);
            try {
                db.delete(utxoCF, key.getBytes());
            }
            catch (RocksDBException e) {
                e.printStackTrace();
            }
        }
        it.close();
    }
}
