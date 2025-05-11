package com.example.blockchain.service;

import com.example.blockchain.model.Block;
import com.example.blockchain.model.Transaction;
import com.example.blockchain.model.TransactionStatus;
import com.example.blockchain.model.UTXO;
import com.example.blockchain.repository.BlockchainRepository;
import com.example.blockchain.repository.TransactionRepository;
import com.example.blockchain.repository.UTXORepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BlockchainService {
    private final BlockchainRepository blockchainRepository;
    private final TransactionRepository transactionRepository;
    private final UTXORepository utxoRepository;
    private final NodeService nodeService;
//    private final UTXOService utxoService;
    private static final int MAX_SEARCH_DEPTH = 100;

    public BlockchainService(BlockchainRepository blockchainRepository, TransactionRepository transactionRepository, UTXORepository utxoRepository, NodeService nodeService, UTXOService utxoService) {
        this.blockchainRepository = blockchainRepository;
        this.transactionRepository = transactionRepository;
        this.utxoRepository = utxoRepository;
//        this.utxoService = utxoService;
        this.nodeService = nodeService;
    }

    public Map<String, Block> getAllBlocks() {
        return blockchainRepository.findAllBlocks();
    }

    public Block getBlock(String hash) {
        return blockchainRepository.findBlock(hash);
    }

    public boolean addBlock(Block block) {
        if (isValid(block)) {
            if (blockchainRepository.saveBlock(block)) {
                System.out.println("[BLOCK ADDED] " + block.getBlockHash());
                if (nodeService.getUnlinkedBlocks().containsKey(block.getBlockHash())){
                    // recursively add all unlinked blocks
                    addBlock(nodeService.getUnlinkedBlocks().get(block.getBlockHash()));
                }
                // after block is saved, check for node's pending transactions
                // if found, change status to "confirmed", delete inputs and add outputs to UTXO
                for(Transaction transaction : block.getTransactions()) {
                    if(blockchainRepository.findBlock(transaction.getTransactionId()) != null){
                        transaction.setStatus(TransactionStatus.CONFIRMED);
                        transactionRepository.saveTransaction(transaction);

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
        }
        return false;
    }

    private boolean isValid(Block block){
        if(!block.validateBlock()){
            System.out.println("[BLOCK NOT VALID] Didn't pass proof of work test (" + block.getBlockHash() + ")");
            return false;
        }

        Block previousBlock = blockchainRepository.findBlock(block.getPreviousHash());

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
                } else {
                    if(blockchainRepository.findBlock(previousBlock.getBlockHash()) == null) {
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

        previousBlock = blockchainRepository.findBlock(block.getPreviousHash());

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
