package com.example.blockchain.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Blockchain {
    private List<Block> blocks;

    public Blockchain(){
        this.blocks = new ArrayList<>();
        Block genesis = new Block(0, "0000aw5d1awd1w89ad1w8a9d", Arrays.asList(), 0, 0, "00000000awdawd");
        this.blocks.add(genesis);
    }
}
