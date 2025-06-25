package com.example.blockchain.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdatePinRequest {
    private String encOldPin;
    private String encNewPin;

    @JsonCreator
    public UpdatePinRequest(@JsonProperty("oldPin") String encOldPin,
                            @JsonProperty("newPin") String encNewPin) {
        this.encOldPin = encOldPin;
        this.encNewPin = encNewPin;
    }

    public String getEncOldPin() {
        return encOldPin;
    }

    public void setEncOldPin(String encOldPin) {
        this.encOldPin = encOldPin;
    }

    public String getEncNewPin() {
        return encNewPin;
    }

    public void setEncNewPin(String encNewPin) {
        this.encNewPin = encNewPin;
    }
}
