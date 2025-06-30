package org.example.desktopclient.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigInteger;

public class MiningBlockContainer {
    private Block blockToMine;
    private BigInteger target;

    @JsonCreator
    public MiningBlockContainer(@JsonProperty("blockToMine") Block blockToMine,
                                @JsonProperty("target") BigInteger target) {
        this.blockToMine = blockToMine;
        this.target = target;
    }

    public Block getBlockToMine() {
        return blockToMine;
    }

    public void setBlockToMine(Block blockToMine) {
        this.blockToMine = blockToMine;
    }

    public BigInteger getTarget() {
        return target;
    }

    public void setTarget(BigInteger target) {
        this.target = target;
    }

    public static String toJson(MiningBlockContainer block){
        try{
            return new ObjectMapper().writeValueAsString(block);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static MiningBlockContainer fromJson(String json){
        try{
            return new ObjectMapper().readValue(json, MiningBlockContainer.class);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
