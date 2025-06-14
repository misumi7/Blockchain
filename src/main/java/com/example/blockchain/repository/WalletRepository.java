package com.example.blockchain.repository;

import com.example.blockchain.model.UTXO;
import com.example.blockchain.model.Wallet;
import org.rocksdb.*;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@Repository
public class WalletRepository{
    private RocksDB db;
    private ColumnFamilyHandle walletCF;
    private ColumnFamilyHandle walletNameCF;

    public WalletRepository(BaseRepository baseRepository) {
        this.db = baseRepository.getDb();
        this.walletCF = baseRepository.getWalletCF();
        this.walletNameCF = baseRepository.getWalletNameCF();
        //deleteAllWallets();
    }

    public synchronized boolean saveWallet(Wallet wallet, String walletName){
        try(WriteBatch batch = new WriteBatch()) {
            batch.put(walletCF, wallet.getPublicKeyBytes(), Wallet.toJson(wallet).getBytes(StandardCharsets.UTF_8));
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

    public synchronized List<Wallet> getAllWallets(){
        List<Wallet> wallets = new ArrayList<>();
        RocksIterator it = db.newIterator(walletCF);
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String publicKey = Base64.getEncoder().encodeToString(it.key());
            //System.out.println("Found wallet: " + publicKey);
            Wallet wallet = Wallet.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            wallets.add(wallet);
        }
        it.close();
        return wallets;
    }

    public Wallet getWallet(String publicKey) {
        try {
            byte[] walletPublicKeyBytes = Base64.getDecoder().decode(publicKey);
            byte[] walletData = db.get(walletCF, walletPublicKeyBytes);
            if (walletData != null) {
                return Wallet.fromJson(new String(walletData, StandardCharsets.UTF_8));
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
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
        byte[] walletPublicKeyBytes = Base64.getDecoder().decode(walletPublicKey);
        return getWalletName(walletPublicKeyBytes);
    }

    /*public String getWalletSalt(String walletPublicKey) {
        try {
            byte[] walletSalt = db.get(Base64.getDecoder().decode(walletPublicKey));
            if(walletSalt != null) {
                return new String(walletSalt, StandardCharsets.UTF_8);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getWalletIV(String walletPublicKey) {
        try {
            byte[] walletSalt = db.get(Base64.getDecoder().decode(walletPublicKey));
            if(walletSalt != null) {
                return new String(walletSalt, StandardCharsets.UTF_8);
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public void deleteAllWallets() {
        try (RocksIterator it = db.newIterator(walletCF)) {
                for (it.seekToFirst(); it.isValid(); it.next()) {
                    try (WriteBatch batch = new WriteBatch()) {
                        String key = Base64.getEncoder().encodeToString(it.key());
                        System.out.println("Deleting wallet with public key: " + key);
                        batch.delete(walletCF, it.key());
                        batch.delete(walletNameCF, it.key());
                        db.write(new WriteOptions(), batch);
                    }
                }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
    }
}
