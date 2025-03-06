package com.example.blockchain.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Blockchain {
    private List<Block> blocks;

    public Blockchain(){
        this.blocks = new ArrayList<>();
        Block genesis = new Block(0, "0", Arrays.asList());
        this.blocks.add(genesis);
    }
}
