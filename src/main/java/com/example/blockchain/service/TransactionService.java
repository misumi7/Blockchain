package com.example.blockchain.service;

import com.example.blockchain.model.*;
import com.example.blockchain.repository.TransactionRepository;
import com.example.blockchain.response.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

@Service
public class TransactionService{
    private final TransactionRepository transactionRepository;
    private final UTXOService utxoService;
    private final NodeService nodeService;
    private final WalletService walletService;

    public TransactionService(TransactionRepository transactionRepository, UTXOService utxoService, NodeService nodeService, WalletService walletService) {
        this.transactionRepository = transactionRepository;
        this.walletService = walletService;
        this.utxoService = utxoService;
        this.nodeService = nodeService;
    }

    public void saveTransaction(Transaction transaction){
        if(!transactionRepository.saveTransaction(transaction)){
            throw new ApiException("Transaction could not be saved", 400);
        }
    }

    public Map<String, Transaction> getTransactions(){
        return transactionRepository.getAllTransactions();
    }

    public Transaction getTransaction(String transactionId){
        return transactionRepository.getTransaction(transactionId);
    }

    public void deleteTransaction(String transactionId) {
        if(!transactionRepository.deleteTransaction(transactionId)){
            throw new ApiException("Transaction could not be deleted", 400);
        }
    }

    public boolean isTransactionValid(Transaction transaction) {
        // add check if the transaction is not expired

        if(verifySignature(transaction)){
            System.out.println("[INVALID TRANSACTION] Transaction signature is invalid");
            return false;
        }

        // Inputs:
        // check if the inputs exist in the UTXO db, are not reserved
        long totalInput = 0;
        for(String input : transaction.getInputs()){
            //double spending check
            if(nodeService.isUTXOAvailable(input)) {
                System.out.println("[INVALID TRANSACTION] Input UTXO is reserved (" + input + ")");
                return false;
            }

            String[] keyParts = input.split(":");
            UTXO utxo = utxoService.getUTXO(keyParts[0], keyParts[1], Integer.parseInt(keyParts[2]));
            if(utxo == null) {
                System.out.println("[INVALID TRANSACTION] Input UTXO does not exist (" + input + ")");
                return false;
            }
            if(utxo.getOwner() != transaction.getSenderPublicKey()) {
                System.out.println("[INVALID TRANSACTION] Input UTXO does not belong to the sender (" + input + ")");
                return false;
            }

            totalInput += utxo.getAmount();

            // Outputs:
            long totalRest = 0, totalOutput = 0;
            for(UTXO output : transaction.getOutputs()){
                if(output.getOwner() == transaction.getSenderPublicKey()){
                    totalRest += output.getAmount();
                }
                else if(output.getOwner() == transaction.getReceiverPublicKey()){
                    totalOutput += output.getAmount();
                }
                else{
                    System.out.println("[INVALID TRANSACTION] Output UTXO does not belong to the sender or receiver (" + output.getKey() + ")");
                    return false;
                }
            }

            if(totalRest != transaction.getAmount() - totalInput){
                System.out.println("[INVALID TRANSACTION] Incorrect change amount (" + totalRest + " != " + (transaction.getAmount() - totalInput) + ")");
                return false;
            }
            if(totalOutput != transaction.getAmount()){
                System.out.println("[INVALID TRANSACTION] Output UTXO does not match the transaction amount (" + totalOutput + " != " + transaction.getAmount() + ")");
                return false;
            }
        }

        if(totalInput < transaction.getAmount()) {
            System.out.println("[INVALID TRANSACTION] Not enough input UTXOs to cover the transaction amount");
            return false;
        }
        return true;
    }

    public void signTransaction(Transaction transaction){
        try{
            byte[] privateKeyBytes = walletService.getPrivateKey();
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = KeyFactory.getInstance("EC", "BC").generatePrivate(privateSpec);

            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initSign(privateKey);
            signature.update(transaction.getTransactionId().getBytes(StandardCharsets.UTF_8));
            transaction.setDigitalSignature(signature.sign());
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException e){
            System.out.println("[SIGNATURE ERROR] No such algorithm or provider");
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean verifySignature(Transaction transaction) {
        try {
            byte[] publicKeyBytes = Base64.getDecoder().decode(transaction.getSenderPublicKey());
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            PublicKey publicKey = KeyFactory.getInstance("EC", "BC").generatePublic(publicSpec);

            Signature signature  = Signature.getInstance("SHA256withECDSA", "BC");
            signature.initVerify(publicKey);
            signature.update(transaction.getTransactionId().getBytes(StandardCharsets.UTF_8));
            return signature.verify(transaction.getDigitalSignature());
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException e){
            System.out.println("[SIGNATURE ERROR] No such algorithm or provider");
            e.printStackTrace();
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createTransaction(String senderPublicKey, String receiverPublicKey, double coinAmount) {
        // Required utxo are selected from the existing ones
        // We create output utxo for the receiver
        // Then we create the transaction and validate it (if input utxo exists and both input and output utxo are valid)
        // We save the transaction and output utxo in local db, delete the input utxo from db and send the transaction to out neighbors
        // all validation should be done in the node service class, here we just create the transaction

        // Create raw transaction
        long amount = (long) (coinAmount * 100000000);
        List<UTXO> ownerUTXO = utxoService.getUtxoByOwner(senderPublicKey);
        System.out.println("Owner UTXO: \n" + ownerUTXO);
        long selectedAmount = 0;
        List<String> inputs = new ArrayList<>();
        for(UTXO utxo : ownerUTXO) {
            if(utxo.getAmount() + selectedAmount >= amount) {
                break;
            }
            selectedAmount += utxo.getAmount();
            inputs.add(utxo.getKey());
        }
        System.out.println("Selected UTXO: \n" + inputs);
        if(selectedAmount < amount) {
            System.out.println("[TRANSACTION ERROR] Not enough UTXO to create transaction");
            return;
        }

        Transaction transaction = new Transaction(inputs, null, Instant.now().toEpochMilli(), amount, senderPublicKey, receiverPublicKey, TransactionStatus.PENDING);
        List<UTXO> outputs = new ArrayList<>();
        if(selectedAmount > amount){
            UTXO rest = new UTXO(transaction.calculateHash(), outputs.size(), senderPublicKey, selectedAmount - amount);
            outputs.add(rest);
        }
        UTXO output = new UTXO(transaction.calculateHash(), outputs.size(), receiverPublicKey, amount);
        outputs.add(output);

        transaction.setOutputs(outputs);
        signTransaction(transaction);

        // Send transaction to neighbors (and wait for confirmation)
        boolean wasTransactionAccepted = nodeService.sendTransaction(transaction);

        if(wasTransactionAccepted){
            transactionRepository.saveTransaction(transaction);
            for(String utxoKey : inputs){
                nodeService.reserveUTXO(utxoKey);
            }
        }
        else{
            System.out.println("[TRANSACTION ERROR] Transaction was not accepted by the network");
            transaction.setStatus(TransactionStatus.REJECTED);
            transactionRepository.saveTransaction(transaction);
        }
    }

//    public String calculateHash(String... inputs){
//        StringBuilder stringToHash = new StringBuilder();
//        for(String input : inputs){
//            stringToHash.append(input);
//        }
//        try{
//            MessageDigest digest = MessageDigest.getInstance("SHA-256");
//            byte[] hash = digest.digest(stringToHash.toString().getBytes(StandardCharsets.UTF_8));
//
//            StringBuilder hexString = new StringBuilder();
//            for (byte b : hash) {
//                // 0xff = 11111111
//                // 0xff & b for b to be treated as an unsigned 8-bit value
//                String hex = Integer.toHexString(0xff & b);
//                // 2 digits, if 1 then append 0
//                if (hex.length() == 1) {
//                    hexString.append('0');
//                }
//                hexString.append(hex);
//            }
//            return hexString.toString();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//            return null;
//        }
//    }

    public boolean changeTransactionStatus(String txId, TransactionStatus status) {
        Transaction transaction = transactionRepository.getTransaction(txId);
        if(transaction == null || transaction.getStatus() == status) {
            return false;
        }
        transaction.setStatus(status);
        transactionRepository.saveTransaction(transaction);
        return true;
    }
}
