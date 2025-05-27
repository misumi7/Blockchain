package com.example.blockchain.service;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.model.TransactionStatus;
import com.example.blockchain.model.UTXO;
import com.example.blockchain.repository.BlockchainRepository;
import com.example.blockchain.repository.TransactionRepository;
import com.example.blockchain.repository.UTXORepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BlockchainService {
    private final BlockchainRepository blockchainRepository;
    private final TransactionRepository transactionRepository;
    private final UTXORepository utxoRepository;
    private final NodeService nodeService;
    private final TransactionService transactionService;
    // private final UTXOService utxoService;
    private static final int MAX_SEARCH_DEPTH = 100;

    public BlockchainService(BlockchainRepository blockchainRepository, TransactionRepository transactionRepository, UTXORepository utxoRepository, NodeService nodeService, TransactionService transactionService) {
        this.blockchainRepository = blockchainRepository;
        this.transactionRepository = transactionRepository;
        this.utxoRepository = utxoRepository;
        // this.utxoService = utxoService;
        this.transactionService = transactionService;
        this.nodeService = nodeService;

        // TEMP::
        /*blockchainRepository.deleteAllBlocks();
        utxoRepository.deleteAllUTXO();*/

        // Add genesis block if the blockchain is empty
        if(getAllBlocks().isEmpty()){
            addGenesisBlock();
        }
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void discoverNewBlocks(){
        Map<Set<Block>, Integer> blockLists = getNewBlocksFromPeers(getLatestBlockIndex());
        addLongestChain(blockLists);
    }

    private void addLongestChain(Map<Set<Block>, Integer> blockLists) {
        if(blockLists.isEmpty()){
            System.out.println("[BLOCK SEARCH] No new blocks found from peers");
            return;
        }
        Set<Block> longestChain = Collections.max(blockLists.keySet(), Comparator.comparingInt(blockLists::get));
        List<Block> sortedLongestChain =  longestChain.stream()
                .sorted(Comparator.comparing(e -> e.getIndex()))
                .collect(Collectors.toList());
        System.out.println("[BLOCK SEARCH] Found " + sortedLongestChain.size() + " new blocks from peers");
        boolean isChainValid = true;
        for(Block block : sortedLongestChain) {
            if(!isValid(block)) {
                System.out.println("[BLOCK SEARCH] Invalid block found: " + block.getBlockHash());
                isChainValid = false;
            }
        }
        if(isChainValid) {
            for(Block block : sortedLongestChain) {
                addBlock(block);
            }
            System.out.println("[BLOCK SEARCH] Added " + sortedLongestChain.size() + " new blocks to the blockchain");
        }
        else {
            System.out.println("[BLOCK SEARCH] Longest chain is invalid, trying to find another chain");
            blockLists.remove(longestChain);
            addLongestChain(blockLists);
        }
    }

    public long getLatestBlockIndex(){
        return blockchainRepository.getLatestBlockIndex();
    }

    public Map<Set<Block>, Integer> getNewBlocksFromPeers(long latestBlockIndex){
        Map<Set<Block>, Integer> blockLists = new ConcurrentHashMap<>();
        for(String peer : nodeService.getPeers()){
            Runnable discoverNewBlocksFromPeer = () -> {
                Set<Block> blocks = new HashSet<>();
                boolean isChainValid = true;
                try {
                    String url = peer + "/api/blocks/latest";
                    Block latestBlock = nodeService.getRestTemplate().getForObject(url, Block.class);
                    if(latestBlock != null && latestBlock.getIndex() > latestBlockIndex) {
                        blocks.add(latestBlock);
                        for(long i = latestBlock.getIndex(); latestBlock.getIndex() > latestBlockIndex; --i) {
                            url = peer + "/api/blocks/" + latestBlock.getPreviousHash();
                            latestBlock = nodeService.getRestTemplate().getForObject(url, Block.class);
                            if(latestBlock != null && latestBlock.getIndex() == i - 1) {
                                blocks.add(latestBlock);
                            }
                            else {
                                isChainValid = false;
                                break;
                            }
                        }
                        if(isChainValid && !blocks.isEmpty()){
                            if(blockLists.containsKey(blocks)){
                                blockLists.replace(blocks, blockLists.get(blocks) + 1);
                            }
                            else {
                                blockLists.put(blocks, 1);
                            }
                        }
                    }
                }
                catch (Exception e) {
                    System.out.println("[BLOCK SEARCH] Error: " + peer);
                    e.printStackTrace();
                }
            };
            nodeService.getExecutor().execute(discoverNewBlocksFromPeer);
        }
        return blockLists;
    }

    public void addGenesisBlock(){
        final String GENESIS_PUBLIC_KEY = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEIuNKcF1LZJ4geWnLrUDuDkFwjT4XGl7h+Wyi+yEdUhNsI0vrFMjoXyBz0SU7zUrykJafHjsdNlASHXOoIFxfgg==";
        Transaction firstTransaction = new Transaction(
                new ArrayList<>(),
                null,
                0,
                100 * 100000000L,
                null,
                GENESIS_PUBLIC_KEY,
                TransactionStatus.CONFIRMED,
                0
        );
        UTXO firstUTXO = new UTXO(
                firstTransaction.getTransactionId(),
                0,
                GENESIS_PUBLIC_KEY,
                100 * 100000000L
        );
        firstTransaction.setOutputs(Arrays.asList(firstUTXO));
        Block genesisBlock = new Block(
                0,
                "GENESIS",
                Arrays.asList(firstTransaction),
                0,
                0,
                null
        );
        genesisBlock.setBlockHash(genesisBlock.calculateHash());

        utxoRepository.saveUTXO(firstUTXO);
        blockchainRepository.saveBlock(genesisBlock);
    }

    public Map<String, Block> getAllBlocks() {
        return blockchainRepository.getAllBlocks();
    }

    public Block getBlock(String hash) {
        return blockchainRepository.getBlock(hash);
    }

    public Block getBlock(long index) {
        return blockchainRepository.getBlock(index);
    }

    public boolean addBlock(Block block) {
        if (blockchainRepository.getBlock(block.getBlockHash()) != null){
            System.out.println("[BLOCK NOT ADDED] Block already exists: " + block.getBlockHash());
            return false;
        }
        if(!isValid(block)) {
            System.out.println("[BLOCK NOT ADDED] Block is not valid: " + block.getBlockHash());
            return false;
        }
        if (blockchainRepository.saveBlock(block)) {
            System.out.println("[BLOCK ADDED] " + block.getBlockHash());
            if (nodeService.getUnlinkedBlocks().containsKey(block.getBlockHash())){
                // recursively add all unlinked blocks
                addBlock(nodeService.getUnlinkedBlocks().get(block.getBlockHash()));
            }
            // after block is saved, check for our node's pending transactions
            // if found, change status to "confirmed", delete inputs and add outputs to UTXO db
            for(Transaction transaction : block.getTransactions()) {
                if(transactionRepository.getTransaction(transaction.getTransactionId()) != null){
                    transactionService.changeTransactionStatus(transaction.getTransactionId(), TransactionStatus.CONFIRMED);
                    for(String input : transaction.getInputs()) {
                        String[] keyParts = input.split(":");
                        utxoRepository.deleteUTXO(keyParts[0], keyParts[1], Integer.parseInt(keyParts[2]));
                        nodeService.unlockUTXO(input);
                    }
                    for(UTXO output : transaction.getOutputs()) {
                        utxoRepository.saveUTXO(output);
                    }
                }
            }
            return true;
        }
        else {
            System.out.println("[BLOCK NOT ADDED] Failed to save block: " + block.getBlockHash());
            return false;
        }
    }

    private boolean isValid(Block block){
        if(!block.validateBlock()){
            System.out.println("[BLOCK NOT VALID] Didn't pass proof of work test (" + block.getBlockHash() + ")");
            return false;
        }

        Block previousBlock = blockchainRepository.getBlock(block.getPreviousHash());

        // Previous block wasn't found => can't validate => ask for previous blocks from neighbours
        if(previousBlock == null) {
            Deque<Block> previousBlocks = new ArrayDeque<>();
            Block tempBlock = block;
            for(int depth = 0; depth < MAX_SEARCH_DEPTH; depth++) {
                previousBlock = nodeService.getBlockFromPeers(tempBlock.getPreviousHash());
                if (previousBlock == null) {
                    nodeService.getUnlinkedBlocks().put(tempBlock.getPreviousHash(), tempBlock);
                    System.out.println("[TEMP SAVED] Previous block not found (" + block.getBlockHash()  + ")");
                    return false;
                }
                else {
                    if(blockchainRepository.getBlock(previousBlock.getBlockHash()) == null) {
                        previousBlocks.add(previousBlock);
                        tempBlock = previousBlock;
                    }
                    else
                        break;
                }
            }
            for(Iterator descItr = previousBlocks.descendingIterator(); descItr.hasNext();) {
                Block blockToAdd = (Block)descItr.next();
                addBlock(blockToAdd);
            }
        }

        previousBlock = blockchainRepository.getBlock(block.getPreviousHash());

        // !! For testing previousBlock.calculateHash() => previousBlock.getBlockHash() as previousBlock is hardcoded
        if(!Objects.equals(previousBlock.calculateHash(), block.getPreviousHash())){
            System.out.println("[BLOCK NOT VALID] Previous hash didn't match (" + block.getBlockHash() + ")");
            return false;
        }
        return true;
    }

    public boolean deleteBlock(String hash) {
        return blockchainRepository.deleteBlock(hash);
    }

    public boolean deleteAllBlocks() {
        return blockchainRepository.deleteAllBlocks();
    }
}
