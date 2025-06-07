package com.example.blockchain.repository;

import com.example.blockchain.controller.TransactionController;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.model.UTXO;
import org.rocksdb.*;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class TransactionRepository{
    private RocksDB db;
    private ColumnFamilyHandle transactionCF;

    public TransactionRepository(BaseRepository baseRepository) {
        this.db = baseRepository.getDb();
        this.transactionCF = baseRepository.getTransactionCF();
    }

    public synchronized boolean saveTransaction(Transaction transaction){
        try{
            db.put(transactionCF, transaction.getTransactionId().getBytes(), Transaction.toJson(transaction).getBytes());
            return true;
        }
        catch (RocksDBException e){
            e.printStackTrace();
            return false;
        }
    }

    public synchronized Map<String, Transaction> getAllTransactions(){
        Map<String, Transaction> transactions = new HashMap<>();
        RocksIterator it = db.newIterator(transactionCF);
        for(it.seekToFirst(); it.isValid(); it.next()) {
            String key = new String(it.key(), StandardCharsets.UTF_8);
            Transaction value = Transaction.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            transactions.put(key, value);
        }
        it.close();
        return transactions;
    }

    public synchronized Transaction getTransaction(String transactionId){
        try {
            byte[] transactionBytes = db.get(transactionCF, transactionId.getBytes());
            if(transactionBytes != null){
                return Transaction.fromJson(new String(transactionBytes, StandardCharsets.UTF_8));
            }
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized boolean deleteTransaction(String transactionId) {
        try {
            db.delete(transactionCF, transactionId.getBytes());
            return true;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public synchronized boolean deleteAllTransactions() {
        try (WriteBatch batch = new WriteBatch()) {
            RocksIterator it = db.newIterator(transactionCF);
            for (it.seekToFirst(); it.isValid(); it.next()) {
                batch.delete(transactionCF, it.key());
            }
            db.write(new WriteOptions(), batch);
            return true;
        } catch (RocksDBException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> getTransactionsByWallet(String walletPublicKey) {
        List<Transaction> transactions = new ArrayList<>();
        RocksIterator it = db.newIterator(transactionCF);
        for(it.seekToFirst(); it.isValid(); it.next()) {
            Transaction transaction = Transaction.fromJson(new String(it.value(), StandardCharsets.UTF_8));
            if(transaction != null && (transaction.getSenderPublicKey().equals(walletPublicKey) ||
               transaction.getReceiverPublicKey().equals(walletPublicKey))) {
                transactions.add(transaction);
            }
        }
        it.close();
        return transactions;
    }
}
