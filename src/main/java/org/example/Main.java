package org.example;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Block block = new Block("0000aw5d1awd1w89ad1w8a9d", Arrays.asList(new Transaction(), new Transaction()));
        System.out.println(block.calculateHash());
    }
}