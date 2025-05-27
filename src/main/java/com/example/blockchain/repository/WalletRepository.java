package com.example.blockchain.repository;

import com.example.blockchain.model.UTXO;
import com.example.blockchain.model.Wallet;
import org.rocksdb.*;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Repository
public class WalletRepository{
    private RocksDB db;
    private ColumnFamilyHandle walletCF;

    public WalletRepository(BaseRepository baseRepository) {
        this.db = baseRepository.getDb();
        this.walletCF = baseRepository.getWalletCF();
    }

    public synchronized boolean saveWallet(Wallet wallet){
        try {
            db.put(walletCF, wallet.getPublicKeyObject().getEncoded(), wallet.getPrivateKeyObject().getEncoded());
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean deleteWallet(Wallet wallet){
        try {
            db.delete(walletCF, wallet.getPublicKeyObject().getEncoded());
            return true;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized Map<byte[], byte[]> getAllWallets(){
        Map<byte[], byte[]> wallets = new HashMap<>();
        RocksIterator it = db.newIterator(walletCF);
        for(it.seekToFirst(); it.isValid(); it.next()) {
            byte[] publicKeyBytes = it.key();
            byte[] privateKeyBytes = it.value();
            wallets.put(publicKeyBytes, privateKeyBytes);
        }
        it.close();
        return wallets;
    }
}
