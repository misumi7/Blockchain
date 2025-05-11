package com.example.blockchain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public class Block {
    private final List<Transaction> TRANSACTIONS;
    private final String PREVIOUS_HASH;
    private final long timeStamp;
    private String blockHash;
    private long index;
    private long nonce;

    @JsonCreator
    public Block(@JsonProperty("index") long index,
                 @JsonProperty("previousHash") String previousHash,
                 @JsonProperty("transactions") List<Transaction> transactions,
                 @JsonProperty("timeStamp") long timeStamp,
                 @JsonProperty("nonce") long nonce,
                 @JsonProperty("blockHash") String blockHash) {
        this.TRANSACTIONS = transactions != null ? transactions : new ArrayList<>();
        this.PREVIOUS_HASH = previousHash;
        this.timeStamp = timeStamp;
        this.nonce = nonce;
        this.index = index;
        this.blockHash = blockHash;
    }

    public String calculateHash(){
        String stringToHash = Long.toString(this.index)
                + this.PREVIOUS_HASH
                + Long.toString(this.timeStamp)
                + Long.toString(this.nonce)
                + this.TRANSACTIONS.toString();
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

    public boolean validateBlock(){
               // TEMP!!!
        return /*this.blockHash.equals(this.calculateHash()) &&*/ this.blockHash.startsWith("0000");
    }

    public boolean mineBlock(){
        while(!this.blockHash.startsWith("0000")){
            this.nonce++;
            this.blockHash = this.calculateHash();
            //System.out.println("Mining: " + this.blockHash + " with nonce: " + this.nonce);
        }
        return true;
    }

    public static String toJson(Block block){
        try{
            return new ObjectMapper().writeValueAsString(block);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Block fromJson(String json){
        try{
            return new ObjectMapper().readValue(json, Block.class);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public List<Transaction> getTransactions() {
        return TRANSACTIONS;
    }

    public String getPreviousHash() {
        return PREVIOUS_HASH;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
