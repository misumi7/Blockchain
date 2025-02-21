package org.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block {
    private final List<Transaction> TRANSACTIONS;
    private final String PREVIOUS_HASH;
    private final long timeStamp;
    private String blockHash;
    private long index;
    private long nonce;

    public Block(String previousHash, List<Transaction> transactions) {
        this.TRANSACTIONS = new ArrayList<>(transactions);;
        this.PREVIOUS_HASH = previousHash;
        this.timeStamp = System.currentTimeMillis();
        this.nonce = 0;
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
}
