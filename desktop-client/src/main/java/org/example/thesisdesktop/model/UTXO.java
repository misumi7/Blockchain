package org.example.thesisdesktop.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;

public class UTXO implements Serializable {
    private final String txId;
    private final int outputIndex;
    private final String owner;
    private final long amount;

    @JsonCreator
    public UTXO(@JsonProperty("txId") String txId,
                @JsonProperty("outputIndex") int outputIndex,
                @JsonProperty("owner") String owner,
                @JsonProperty("amount") long amount) {
        this.txId = txId;
        this.outputIndex = outputIndex;
        this.owner = owner;
        this.amount = amount;
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

    public long getAmount() {
        return amount;
    }

    @JsonIgnore
    public String getKey() {
        return owner + ":" + txId + ":" + outputIndex;
    }

    @Override
    public String toString() {
        return "UTXO{" +
                "txId='" + txId + '\'' +
                ", outputIndex=" + outputIndex +
                ", owner='" + owner + '\'' +
                ", amount=" + amount +
                '}';
    }
}
