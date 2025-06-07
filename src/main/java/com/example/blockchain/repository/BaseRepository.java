package com.example.blockchain.repository;

import org.rocksdb.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class BaseRepository {
    public static final String NAME = "data";
    public File dbDir;
    private ColumnFamilyHandle blockCF;
    private ColumnFamilyHandle blockIndexCF;
    private ColumnFamilyHandle utxoCF;
    private ColumnFamilyHandle transactionCF;
    private ColumnFamilyHandle walletCF;
    private ColumnFamilyHandle walletNameCF;
    private RocksDB db;

    public BaseRepository(){
        RocksDB.loadLibrary();
        dbDir = new File(System.getProperty("user.dir") + "/", NAME);
        try {
            final DBOptions dbOptions = new DBOptions().setCreateIfMissing(true).setCreateMissingColumnFamilies(true);

            List<ColumnFamilyDescriptor> cfDescriptors = Arrays.asList(
                    new ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY),
                    new ColumnFamilyDescriptor("block".getBytes()),
                    new ColumnFamilyDescriptor("blockIndex".getBytes()),
                    new ColumnFamilyDescriptor("utxo".getBytes()),
                    new ColumnFamilyDescriptor("transaction".getBytes()),
                    new ColumnFamilyDescriptor("wallet".getBytes()),
                    new ColumnFamilyDescriptor("walletName".getBytes())
            );
            List<ColumnFamilyHandle> cfHandles = new ArrayList<>();
            this.db = RocksDB.open(dbOptions, dbDir.getAbsolutePath(), cfDescriptors, cfHandles);

            this.blockCF = cfHandles.get(1);
            this.blockIndexCF = cfHandles.get(2);
            this.utxoCF = cfHandles.get(3);
            this.transactionCF = cfHandles.get(4);
            this.walletCF = cfHandles.get(5);
            this.walletNameCF = cfHandles.get(6);
        }
        catch (RocksDBException e) {
            e.printStackTrace();
        }
    }

    public ColumnFamilyHandle getBlockCF() {
        return blockCF;
    }

    public ColumnFamilyHandle getBlockIndexCF() {
        return blockIndexCF;
    }

    public ColumnFamilyHandle getUtxoCF() {
        return utxoCF;
    }

    public ColumnFamilyHandle getTransactionCF() {
        return transactionCF;
    }

    public ColumnFamilyHandle getWalletCF() {
        return walletCF;
    }

    public ColumnFamilyHandle getWalletNameCF() {
        return walletNameCF;
    }

    public RocksDB getDb() {
        return db;
    }
}
