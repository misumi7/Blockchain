package com.example.blockchain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Set;

public class Transaction {
    private String transactionId;
    private final String senderPublicKey;
    private final String receiverPublicKey;
    private final List<String> inputs; // "txId:OutputIndex" to de spent (& deleted from the UTXO db)
    private List<UTXO> outputs; // to be added
    private final long timeStamp;
    private final long amount;
    private TransactionStatus status; // "pending", "confirmed", "rejected"
    private byte[] digitalSignature;

    @JsonCreator
    public Transaction(/*@JsonProperty("transactionId") String transactionId,*/
                       @JsonProperty("inputs") List<String> inputs,
                       @JsonProperty("outputs") List<UTXO> outputs,
                       @JsonProperty("timeStamp") long timeStamp,
                       @JsonProperty("amount") long amount,
                       @JsonProperty("senderPublicKey") String senderPublicKey,
                       @JsonProperty("receiverPublicKey") String receiverPublicKey,
                       @JsonProperty("status") TransactionStatus status) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.timeStamp = timeStamp;
        this.amount = amount;
        this.senderPublicKey = senderPublicKey;
        this.receiverPublicKey = receiverPublicKey;
        this.transactionId = calculateHash();
    }

    public String calculateHash(){
        String stringToHash = this.inputs.toString()
                + this.senderPublicKey
                + this.receiverPublicKey
                + this.amount
                + this.timeStamp;
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));

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
    }

    public static String toJson(Transaction transaction){
        try{
            return new ObjectMapper().writeValueAsString(transaction);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Transaction fromJson(String json){
        try{
            return new ObjectMapper().readValue(json, Transaction.class);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public void setDigitalSignature(byte[] digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public String getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public void recalculateTransactionId(){
        this.transactionId = this.calculateHash();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public List<String> getInputs() {
        return inputs;
    }

    public List<UTXO> getOutputs() {
        return outputs;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    public byte[] getDigitalSignature() {
        return digitalSignature;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setOutputs(List<UTXO> outputs) {
        this.outputs = outputs;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }
}
