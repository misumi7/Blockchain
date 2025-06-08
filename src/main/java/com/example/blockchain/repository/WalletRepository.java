package com.example.blockchain.repository;

import com.example.blockchain.model.UTXO;
import com.example.blockchain.model.Wallet;
import org.rocksdb.*;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Repository
public class WalletRepository{
    private RocksDB db;
    private ColumnFamilyHandle walletCF;
    private ColumnFamilyHandle walletNameCF;

    public WalletRepository(BaseRepository baseRepository) {
        this.db = baseRepository.getDb();
        this.walletCF = baseRepository.getWalletCF();
        this.walletNameCF = baseRepository.getWalletNameCF();
    }

    public synchronized boolean saveWallet(Wallet wallet, String walletName){
        try(WriteBatch batch = new WriteBatch()) {
            batch.put(walletCF, wallet.getPublicKeyBytes(), wallet.getPrivateKeyBytes());
            batch.put(walletNameCF, wallet.getPublicKeyBytes(), walletName.getBytes(StandardCharsets.UTF_8));
            db.write(new WriteOptions(), batch);
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean deleteWallet(Wallet wallet){
        try(WriteBatch batch = new WriteBatch()) {
            batch.delete(walletCF, wallet.getPublicKeyBytes());
            batch.delete(walletNameCF, wallet.getPublicKeyBytes());
            db.write(new WriteOptions(), batch);
            return true;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized Map<String, String> getAllWallets(){
        Map<String, String> wallets = new HashMap<>();
        RocksIterator it = db.newIterator(walletCF);
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String publicKey = Base64.getEncoder().encodeToString(it.key());
            String privateKey = Base64.getEncoder().encodeToString(it.value());
            wallets.put(publicKey, privateKey);
        }
        it.close();
        return wallets;
    }

    public String getWalletName(byte[] publicKey) {
        try {
            byte[] walletName = db.get(walletNameCF, publicKey);
            if (walletName != null) {
                return new String(walletName, StandardCharsets.UTF_8);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getWalletName(String walletPublicKey) {
        try {
            byte[] walletPublicKeyBytes = Base64.getDecoder().decode(walletPublicKey);
            byte[] walletName = db.get(walletNameCF, walletPublicKeyBytes);
            if (walletName != null) {
                return new String(walletName, StandardCharsets.UTF_8);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }
}
