package com.example.blockchain.service;

import com.example.blockchain.model.*;
import com.example.blockchain.repository.TransactionRepository;
import com.example.blockchain.request.SignedTransactionRequest;
import com.example.blockchain.request.TransactionRequest;
import com.example.blockchain.response.ApiException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

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

        // resend pending transactions (in case the node was offline)
        Map<String, Transaction> pendingTransactions = transactionRepository.getAllTransactions();
        for(Transaction transaction : pendingTransactions.values()){
            if(transaction.getStatus() == TransactionStatus.PENDING){
                nodeService.addTransactionToMemPool(transaction);
                if(!nodeService.sendTransaction(transaction)) {
                    System.out.println("[TRANSACTION RESEND] Transaction was not accepted by the network: " + transaction.getTransactionId());
                    transaction.setStatus(TransactionStatus.REJECTED);
                    transactionRepository.saveTransaction(transaction);

                    for (String utxo : transaction.getInputs()){
                        nodeService.unlockUTXO(utxo);
                    }

                    nodeService.removeTransactionFromMemPool(transaction);
                }
                else{
                    System.out.println("[TRANSACTION RESEND] Transaction accepted: " + transaction.getTransactionId());
                }
            }
        }

        //transactionRepository.deleteAllTransactions();
    }

    // Tests here:
    @EventListener(ApplicationReadyEvent.class)
    public void test() {
        /*createTransaction(
                walletService.getPublicKey(),
                new byte[] {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05},
                10,
                "0"
        );*/
    }

    public void saveTransactionToMemPool(Transaction transaction){
        if(!isTransactionValid(transaction)) {
            System.out.println("[TRANSACTION SAVE] Transaction is not valid: " + transaction.getTransactionId());
            throw new ApiException("Transaction is not valid", 400);
        }
        if(nodeService.memPoolContains(transaction)) {
            System.out.println("[TRANSACTION SAVE] Transaction already exists in mempool: " + transaction.getTransactionId());
            return;
        } // !memPoolContains && transactionContainsReservedUTXO:
        else if(nodeService.containsReservedUTXO(transaction)){
            System.out.println("[TRANSACTION SAVE] Transaction contains reserved UTXO: " + transaction.getTransactionId());
            throw new ApiException("Transaction contains UTXO that are reserved by another pending transaction", 400);
        }

        nodeService.addTransactionToMemPool(transaction);
        System.out.println("[TRANSACTION SAVE] Transaction saved to mempool: " + transaction.getTransactionId());
        for (String input : transaction.getInputs()) {
            nodeService.reserveUTXO(input);
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

        if(!verifySignature(transaction)){
            System.out.println("[INVALID TRANSACTION] Transaction signature is invalid");
            return false;
        }

        // Inputs:
        // check if the inputs exist in the UTXO db, are not reserved
        long totalInput = 0;
        for(String input : transaction.getInputs()) {
            //double spending check
            /*if (nodeService.isUTXOReserved(input)) {
                System.out.println("[INVALID TRANSACTION] Input UTXO is reserved (" + input + ")");
                return false;
            }*/

            String[] keyParts = input.split(":");
            UTXO utxo = utxoService.getUTXO(keyParts[0], keyParts[1], Integer.parseInt(keyParts[2]));
            if (utxo == null) {
                System.out.println("[INVALID TRANSACTION] Input UTXO does not exist (" + input + ")");
                return false;
            }
            if (!utxo.getOwner().equals(transaction.getSenderPublicKey())) {
                System.out.println("[INVALID TRANSACTION] Input UTXO does not belong to the sender (\n\tUTXO owner:" + utxo.getOwner() + "\n\tTransaction sender" + transaction.getSenderPublicKey() + "\n)");
                return false;
            }

            totalInput += utxo.getAmount();
        }
        if(totalInput < transaction.getAmount()) {
            System.out.println("[INVALID TRANSACTION] Not enough input UTXOs to cover the transaction amount");
            return false;
        }

        // Outputs:
        long totalRest = 0, totalOutput = 0;
        for(UTXO output : transaction.getOutputs()){
            if(output.getOwner().equals(transaction.getSenderPublicKey())){
                totalRest += output.getAmount();
            }
            else if(output.getOwner().equals(transaction.getReceiverPublicKey())){
                totalOutput += output.getAmount();
            }
            else{
                System.out.println("[INVALID TRANSACTION] Output UTXO does not belong to the sender or receiver (" + output.getKey() + ")");
                return false;
            }
        }

        if(totalRest != totalInput - (transaction.getAmount() + transaction.getTransactionFee())){
            System.out.println("[INVALID TRANSACTION] Incorrect change amount (" + totalRest + " != " + (totalInput - (transaction.getAmount() + transaction.getTransactionFee())) + ")");
            return false;
        }
        if(totalOutput != transaction.getAmount()){
            System.out.println("[INVALID TRANSACTION] Output UTXO does not match the transaction amount (" + totalOutput + " != " + transaction.getAmount() + ")");
            return false;
        }
        return true;
    }

    public void signTransaction(Transaction transaction, String senderPublicKey, SecretKeySpec key) {
        try{
            Wallet senderWallet = walletService.getWallet(senderPublicKey);
            byte[] privateKeyBytes = walletService.decryptPrivateKey(senderWallet.getEncryptedPrivateKeyBytes(), senderWallet.getIv(), key);

            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            PrivateKey privateKey = KeyFactory.getInstance("EC", "BC").generatePrivate(privateSpec);

            Signature signature = Signature.getInstance("SHA256withECDSA", "BC");
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

    public void saveWalletTransactions(List<Transaction> transactions) {
        for(Transaction transaction : transactions) {
            if(!transactionRepository.saveTransaction(transaction)) {
                throw new ApiException("Transaction could not be saved", 500);
            }
        }
    }

    public void createSignedTransaction(SignedTransactionRequest transactionRequest) {
        System.out.println(transactionRequest);

        long amount = (long) (transactionRequest.getAmount() * 100000000L);
        String senderPublicKey = transactionRequest.getSenderPublicKey();
        String receiverPublicKey = transactionRequest.getReceiverPublicKey();

        List<UTXO> ownerUTXO = utxoService.getUtxoByOwner(senderPublicKey);
        System.out.println("Owner UTXO: \n" + ownerUTXO);
        long selectedAmount = 0;
        final short feeRate = getOptimalFeeRate();

        Transaction transaction = new Transaction(
                null,
                null,
                transactionRequest.getTimeStamp(),
                transactionRequest.getAmount(),
                senderPublicKey,
                receiverPublicKey,
                TransactionStatus.PENDING,
                transactionRequest.getFee()
        );

        //long feeAmount = (long) feeRate * transaction.getSizeInBytes();
        //transaction.setTransactionFee(feeAmount);

        List<String> inputs = new ArrayList<>();
        for(UTXO utxo : ownerUTXO) {
            if(!nodeService.isUTXOReserved(utxo.getKey())) {
                selectedAmount += utxo.getAmount();
                //feeAmount += (long) feeRate * utxo.getKey().getBytes(StandardCharsets.UTF_8).length;
                inputs.add(utxo.getKey());
                if (selectedAmount >= amount + transactionRequest.getFee()) {
                    break;
                }
            }
        }
        transaction.setTransactionFee(transactionRequest.getFee());
        System.out.println("Selected UTXO: \n" + inputs);
        if(selectedAmount < amount + transactionRequest.getFee()) {
            System.out.println("[TRANSACTION ERROR] Not enough UTXO to create transaction (Try decreasing the fee)");
            throw new ApiException("Not enough UTXO to create transaction (Try decreasing the fee)", 400);
        }

        transaction.setInputs(inputs);

        List<UTXO> outputs = new ArrayList<>();
        if(selectedAmount > amount){
            UTXO rest = new UTXO(transaction.calculateHash(), outputs.size(), senderPublicKey, selectedAmount - (amount + transactionRequest.getFee()));
            outputs.add(rest);
        }
        UTXO output = new UTXO(transaction.calculateHash(), outputs.size(), receiverPublicKey, amount);
        outputs.add(output);

        transaction.setOutputs(outputs);
        if(!transaction.calculateHash().equals(transactionRequest.getTxId())){
            System.out.println("[TRANSACTION ERROR] Transaction ID does not match the request");
            throw new ApiException("Transaction ID does not match the request", 400);
        }
        transaction.setTransactionId(transactionRequest.getTxId());
        //System.out.println("transactionRequest.getEncryptedPin(): " + transactionRequest.getEncryptedPin());
        //String strKey = walletService.decryptPin(Base64.getDecoder().decode(transactionRequest.getEncryptedPin()), walletService.getRSAPrivateKey());
        //System.out.println("Decrypted PIN: " + strKey);

        // pin -> kdf -> aes key
        //Wallet senderWallet = walletService.getWallet(senderPublicKey);
        //SecretKeySpec key = walletService.deriveKey(strKey, senderWallet.getSalt());

        //signTransaction(transaction, transactionRequest.getSenderPublicKey(), key);
        transaction.setDigitalSignature(Base64.getDecoder().decode(transactionRequest.getSignature()));
        if(!verifySignature(transaction)) {
            System.out.println("[TRANSACTION ERROR] Transaction signature is invalid");
            throw new ApiException("Transaction signature is invalid", 400);
        }

        // Send transaction to neighbors (and wait for confirmation)
        boolean wasTransactionAccepted = nodeService.sendTransaction(transaction);

        if(wasTransactionAccepted){
            System.out.println("[TRANSACTION ACCEPTED] " + transaction.getTransactionId());
            transactionRepository.saveTransaction(transaction);
            for(String utxoKey : inputs){
                nodeService.reserveUTXO(utxoKey);
            }
            nodeService.addTransactionToMemPool(transaction);
        }
        else{
            System.out.println("[TRANSACTION ERROR] Transaction was not accepted by the network");
            transaction.setStatus(TransactionStatus.REJECTED);
            transactionRepository.saveTransaction(transaction);
            throw new ApiException("Transaction was not accepted by the network", 400);
        }
    }

    public void createTransaction(TransactionRequest transactionRequest) {
        // Required utxo are selected from the existing ones
        // We create output utxo for the receiver
        // Then we create the transaction and validate it (if input utxo exists and both input and output utxo are valid)
        // We save the transaction and output utxo in local db, delete the input utxo from db and send the transaction to out neighbors
        // all validation should be done in the node service class, here we just create the transaction

        // Create raw transaction
        /*String senderPublicKey = Base64.getEncoder().encodeToString(senderBytePublicKey);
        String receiverPublicKey = Base64.getEncoder().encodeToString(receiverBytePublicKey);*/

        System.out.println(transactionRequest);
        // VALIDATE PIN HERE

        long amount = (long) (transactionRequest.getAmount() * 100000000L);
        String senderPublicKey = transactionRequest.getSenderPublicKey();
        String receiverPublicKey = transactionRequest.getReceiverPublicKey();

        List<UTXO> ownerUTXO = utxoService.getUtxoByOwner(senderPublicKey);
        System.out.println("Owner UTXO: \n" + ownerUTXO);
        long selectedAmount = 0;
        final short feeRate = getOptimalFeeRate();

        Transaction transaction = new Transaction(
                null,
                null,
                Instant.now().toEpochMilli(),
                amount,
                senderPublicKey,
                receiverPublicKey,
                TransactionStatus.PENDING,
                0
        );

        long feeAmount = (long) feeRate * transaction.getSizeInBytes();
        //transaction.setTransactionFee(feeAmount);

        List<String> inputs = new ArrayList<>();
        for(UTXO utxo : ownerUTXO) {
            if(!nodeService.isUTXOReserved(utxo.getKey())) {
                selectedAmount += utxo.getAmount();
                feeAmount += (long) feeRate * utxo.getKey().getBytes(StandardCharsets.UTF_8).length;
                inputs.add(utxo.getKey());
                if (selectedAmount >= amount + feeAmount) {
                    break;
                }
            }
        }
        transaction.setTransactionFee(feeAmount);
        System.out.println("Selected UTXO: \n" + inputs);
        if(selectedAmount < amount + feeAmount) {
            System.out.println("[TRANSACTION ERROR] Not enough UTXO to create transaction (Try decreasing the fee)");
            throw new ApiException("Not enough UTXO to create transaction (Try decreasing the fee)", 400);
        }

        transaction.setInputs(inputs);

        List<UTXO> outputs = new ArrayList<>();
        if(selectedAmount > amount){
            UTXO rest = new UTXO(transaction.calculateHash(), outputs.size(), senderPublicKey, selectedAmount - (amount + feeAmount));
            outputs.add(rest);
        }
        UTXO output = new UTXO(transaction.calculateHash(), outputs.size(), receiverPublicKey, amount);
        outputs.add(output);

        transaction.setOutputs(outputs);
        transaction.setTransactionId(transaction.calculateHash());
        //System.out.println("transactionRequest.getEncryptedPin(): " + transactionRequest.getEncryptedPin());
        byte[] aesKey = walletService.decryptPinBytes(Base64.getDecoder().decode(transactionRequest.getEncryptedPin()), walletService.getRSAPrivateKey());
        //System.out.println("Decrypted PIN: " + strKey);

        // pin -> kdf -> aes key
        //Wallet senderWallet = walletService.getWallet(senderPublicKey);
        SecretKeySpec key = new SecretKeySpec(aesKey, "AES");

        signTransaction(transaction, transactionRequest.getSenderPublicKey(), key);
        if(!verifySignature(transaction)) {
            System.out.println("[TRANSACTION ERROR] Transaction signature is invalid");
            throw new ApiException("Transaction signature is invalid", 400);
        }

        // Send transaction to neighbors (and wait for confirmation)
        boolean wasTransactionAccepted = nodeService.sendTransaction(transaction);

        if(wasTransactionAccepted){
            System.out.println("[TRANSACTION ACCEPTED] " + transaction.getTransactionId());
            transactionRepository.saveTransaction(transaction);
            for(String utxoKey : inputs){
                nodeService.reserveUTXO(utxoKey);
            }
            nodeService.addTransactionToMemPool(transaction);
        }
        else{
            System.out.println("[TRANSACTION ERROR] Transaction was not accepted by the network");
            transaction.setStatus(TransactionStatus.REJECTED);
            transactionRepository.saveTransaction(transaction);
            throw new ApiException("Transaction was not accepted by the network", 400);
        }
    }

    public short getOptimalFeeRate() {
        return (short) min(100, max(10, nodeService.getMemPool().size() / nodeService.getAvgMemPoolSize() * 100));
    }

    /*public String calculateHash(String... inputs){
        StringBuilder stringToHash = new StringBuilder();
        for(String input : inputs){
            stringToHash.append(input);
        }
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(stringToHash.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                // 0xff = 11111111
                // 0xff & b for b to be treated as an unsigned 8-bit value
                String hex = Integer.toHexString(0xff & b);
                // 2 digits, if 1 then append 0
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }*/

    public boolean changeTransactionStatus(String txId, TransactionStatus status) {
        Transaction transaction = transactionRepository.getTransaction(txId);
        if(transaction == null || transaction.getStatus() == status) {
            return false;
        }
        transaction.setStatus(status);
        transactionRepository.saveTransaction(transaction);
        return true;
    }

    public void deleteAllTransactions() {
        transactionRepository.deleteAllTransactions();
    }

    public List<Transaction> getTransactionsByWallet(String walletPublicKey) {
        return transactionRepository.getTransactionsByWallet(walletPublicKey);
    }

    // Checks if the default PIN is set by deriving the pubKey from the enc prKey
    public boolean isDefaultPinSet() {
        Wallet wallet = walletService.getWalletList().getFirst();
        SecretKeySpec derivedKey = walletService.deriveKey(WalletService.DEFAULT_PIN, wallet.getSalt());
        return comparePins(wallet, derivedKey);
    }

    public boolean comparePins(Wallet wallet, SecretKeySpec aesKey) {
        try {
            //SecretKeySpec derivedKey = walletService.deriveKey(pin, wallet.getSalt());

            Transaction testTransaction = new Transaction(new ArrayList<>(), new ArrayList<>(), 0, 0, wallet.getPublicKey(), wallet.getPublicKey(), TransactionStatus.PENDING, 0);
            signTransaction(testTransaction, wallet.getPublicKey(), aesKey);

            return verifySignature(testTransaction);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Error comparing pins", 500);
        }
    }

    public void setPin(String walletPublicKey, String encryptedKey, String newEncryptedKey) {
        byte[] decryptedOldKey = walletService.decryptPinBytes(Base64.getDecoder().decode(encryptedKey), walletService.privateKey);
        byte[] decryptedNewKey = walletService.decryptPinBytes(Base64.getDecoder().decode(newEncryptedKey), walletService.privateKey);

        /*System.out.println("Decrypted PIN: " + decryptedPin);
        System.out.println("Decrypted new PIN: " + decryptedNewPin);*/

        //SecretKeySpec derivedKey = walletService.deriveKey(WalletService.DEFAULT_PIN, walletService.getWalletSalt(walletPublicKey));
        //System.out.println("Derived default key: " + Base64.getEncoder().encodeToString(derivedKey.getEncoded()));
        // key is derived ok => rsa enc problem
        //System.out.println("Decrypted old key: " + Base64.getEncoder().encodeToString(decryptedOldKey));
        // same ok. => problem in pin comparison

        Wallet wallet = walletService.getWallet(walletPublicKey);
        if (!comparePins(wallet, new SecretKeySpec(decryptedOldKey, "AES"))) {
            throw new ApiException("Incorrect PIN received", 400);
        }


        byte[] decryptedWalletPrivateKey = walletService.decryptPrivateKey(
                wallet.getEncryptedPrivateKeyBytes(),
                wallet.getIv(),
                new SecretKeySpec(decryptedOldKey, "AES")
        );

        //SecretKeySpec newDerivedKey = walletService.deriveKey(decryptedNewPin, wallet.getSalt());

        String encryptedPrivateKey = walletService.encryptPrivateKey(decryptedWalletPrivateKey, wallet.getIv(), new SecretKeySpec(decryptedNewKey, "AES"));
        wallet.setEncryptedPrivateKey(encryptedPrivateKey);
        walletService.updateWallet(wallet);
        System.out.println("[PIN UPDATE] Wallet " + wallet.getPublicKey() + " PIN updated successfully");
    }

    public void deleteWalletTransactions(String walletPublicKey) {
        List<Transaction> transactions = transactionRepository.getTransactionsByWallet(walletPublicKey);
        for(Transaction transaction : transactions) {
            if(!transactionRepository.deleteTransaction(transaction.getTransactionId())) {
                throw new ApiException("Transaction could not be deleted", 500);
            }
        }
        System.out.println("[TRANSACTIONS DELETED] All transactions for wallet " + walletPublicKey + " deleted successfully");
    }
}
