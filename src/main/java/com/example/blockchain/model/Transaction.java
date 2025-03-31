package com.example.blockchain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Set;

public class Transaction {
    private final String transactionId;
    private final Set<String> inputs; // "txId-OutputIndex" to de spent (& deleted from the UTXO db)
    private final Set<String> outputs; // to be added
    private final long timeStamp;

    @JsonCreator
    public Transaction(@JsonProperty("transactionId") String transactionId,
                       @JsonProperty("inputs") Set<String> inputs,
                       @JsonProperty("outputs") Set<String> outputs,
                       @JsonProperty("timeStamp") long timeStamp) {
        this.transactionId = transactionId;
        this.inputs = inputs;
        this.outputs = outputs;
        this.timeStamp = timeStamp;
    }

    public String calculateHash(){
        String stringToHash = this.transactionId
                + this.inputs.toString()
                + this.outputs.toString()
                + Long.toString(this.timeStamp);
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

    public String getTransactionId() {
        return transactionId;
    }

    public Set<String> getInputs() {
        return inputs;
    }

    public Set<String> getOutputs() {
        return outputs;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
}
