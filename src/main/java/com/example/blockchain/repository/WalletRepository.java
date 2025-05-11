package com.example.blockchain.repository;

import com.example.blockchain.model.UTXO;
import com.example.blockchain.model.Wallet;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Repository
public class WalletRepository {
    // think about how should be stored private keys in db
    private final static String NAME = "wallet-db";
    File dbDir;
    RocksDB db;

    public WalletRepository() {
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

    public boolean saveWallet(Wallet wallet){
        try {
            db.put(wallet.getPublicKeyObject().getEncoded(), wallet.getPrivateKeyObject().getEncoded());
            return true;
        }
        catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteWallet(Wallet wallet){
        try {
            db.delete(wallet.getPublicKeyObject().getEncoded());
            return true;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<byte[], byte[]> getAllWallets(){
        Map<byte[], byte[]> wallets = new HashMap<>();
        RocksIterator it = db.newIterator();
        for(it.seekToFirst(); it.isValid(); it.next()) {
            byte[] publicKeyBytes = it.key();
            byte[] privateKeyBytes = it.value();
            wallets.put(publicKeyBytes, privateKeyBytes);
        }
        it.close();
        return wallets;
    }
}
