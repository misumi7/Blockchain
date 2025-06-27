package com.example.blockchain.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SignedTransactionRequest {
    private String txId;
    private String senderPublicKey;
    private String receiverPublicKey;
    private long amount;
    private long fee;
    private long timeStamp;
    private String signature;

    @JsonCreator
    public SignedTransactionRequest(
            @JsonProperty("txId") String txId,
            @JsonProperty("senderPublicKey") String senderPublicKey,
            @JsonProperty("receiverPublicKey") String receiverPublicKey,
            @JsonProperty("amount") long amount,
            @JsonProperty("fee") long fee,
            @JsonProperty("timeStamp") long timeStamp,
            @JsonProperty("signature") String signature
    ) {
        this.txId = txId;
        this.senderPublicKey = senderPublicKey;
        this.receiverPublicKey = receiverPublicKey;
        this.amount = amount;
        this.fee = fee;
        this.timeStamp = timeStamp;
        this.signature = signature;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(String txId) {
        this.txId = txId;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    public void setSenderPublicKey(String senderPublicKey) {
        this.senderPublicKey = senderPublicKey;
    }

    public String getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public void setReceiverPublicKey(String receiverPublicKey) {
        this.receiverPublicKey = receiverPublicKey;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getFee() {
        return fee;
    }

    public void setFee(long fee) {
        this.fee = fee;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
