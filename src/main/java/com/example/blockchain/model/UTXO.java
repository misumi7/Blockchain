package com.example.blockchain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class UTXO {
    private final String txId;
    private final int outputIndex;
    private final String owner;
    private final double amount;
    private final boolean spent;

    @JsonCreator
    public UTXO(@JsonProperty("txId") String txId,
                @JsonProperty("outputIndex") int outputIndex,
                @JsonProperty("owner") String owner,
                @JsonProperty("amount") double amount) {
        this.txId = txId;
        this.outputIndex = outputIndex;
        this.owner = owner;
        this.amount = amount;
        this.spent = false;
    }

    public static String toJson(UTXO utxo){
        try{
            return new ObjectMapper().writeValueAsString(utxo);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static UTXO fromJson(String json){
        try{
            return new ObjectMapper().readValue(json, UTXO.class);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public String getTxId() {
        return txId;
    }

    public int getOutputIndex() {
        return outputIndex;
    }

    public String getOwner() {
        return owner;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isSpent() {
        return spent;
    }
}
