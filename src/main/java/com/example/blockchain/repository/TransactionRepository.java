package com.example.blockchain.repository;

import com.example.blockchain.controller.TransactionController;
import com.example.blockchain.model.Transaction;
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
public class TransactionRepository {
    private final static String NAME = "transaction-db";
    File dbDir;
    RocksDB db;

    public TransactionRepository() {
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

    public boolean saveTransaction(Transaction transaction){
        try{
            db.put(transaction.getTransactionId().getBytes(), Transaction.toJson(transaction).getBytes());
            return true;
        }
        catch (RocksDBException e){
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Transaction> getAllTransactions(){
        Map<String, Transaction> transactions = new HashMap<>();
        RocksIterator it = db.newIterator();
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String key = new String(it.key(), StandardCharsets.UTF_8);
            Transaction value = Transaction.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            transactions.put(key, value);
        }
        it.close();
        return transactions;
    }

    public Transaction getTransaction(String transactionId){
        Transaction value = null;
        try {
            value = Transaction.fromJson(new String(db.get(transactionId.getBytes())));
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return value;
    }
}
