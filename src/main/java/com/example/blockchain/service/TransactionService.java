package com.example.blockchain.service;

import com.example.blockchain.model.Transaction;
import com.example.blockchain.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TransactionService{
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    public boolean saveTransaction(Transaction transaction){
        return transactionRepository.saveTransaction(transaction);
    }

    public Map<String, Transaction> getTransactions(){
        return transactionRepository.getAllTransactions();
    }

    public Transaction getTransaction(String transactionId){
        return transactionRepository.getTransaction(transactionId);
    }
}
