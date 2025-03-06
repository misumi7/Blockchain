package com.example.blockchain.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class Transaction {
    private final String transactionId;
    private final List<UTXO> UTXOinputs; // to de spent (deleted from the UTXO pool)
    private final List<UTXO> UTXOoutputs; // to be added
    private final long timeStamp;

    public Transaction(String transactionId, List<UTXO> inputs, List<UTXO> outputs, long timeStamp) {
        this.transactionId = transactionId;
        this.UTXOinputs = inputs;
        this.UTXOoutputs = outputs;
        this.timeStamp = timeStamp;
    }

    public String calculateHash(){
        String stringToHash = this.transactionId
                + this.UTXOinputs.toString()
                + this.UTXOoutputs.toString()
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
}
