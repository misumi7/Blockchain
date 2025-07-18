package com.example.blockchain.service;

import com.example.blockchain.model.*;
import com.example.blockchain.repository.BlockchainRepository;
import com.example.blockchain.repository.TransactionRepository;
import com.example.blockchain.repository.UTXORepository;
import com.example.blockchain.response.ApiException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.lang.Math.max;

@Service
public class BlockchainService {
    public static final BigInteger MAX_TARGET = new BigInteger("2").pow(224).multiply(BigInteger.valueOf(65535));
    public static final BigInteger TARGET_REF = MAX_TARGET.divide(BigInteger.valueOf(100_000L));
    public static final long REFERENCE_TIMESTAMP = LocalDateTime.of(2025, 6, 30, 15, 30).toInstant(ZoneOffset.UTC).toEpochMilli();
    public static final long REFERENCE_HEIGHT = 0;
    public static final int BLOCK_TARGET_TIME = 3 * 60 * 1000; // 1 min (for testing)
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final int INITIAL_REWARD_COINS = 1000;
    public static final int HALVING_STEP_BLOCKS = 1000;
    private final BlockchainRepository blockchainRepository;
    private final TransactionRepository transactionRepository;
    private final UTXOService utxoService;
    private final NodeService nodeService;
    private final TransactionService transactionService;
    private final WalletService walletService;
    // private final UTXOService utxoService;
    private static final int MAX_SEARCH_DEPTH = 100;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();


    public BlockchainService(BlockchainRepository blockchainRepository, TransactionRepository transactionRepository, UTXORepository utxoRepository, NodeService nodeService, TransactionService transactionService, WalletService walletService, UTXOService utxoService) {
        this.blockchainRepository = blockchainRepository;
        this.transactionRepository = transactionRepository;
        this.utxoService = utxoService;
        this.transactionService = transactionService;
        this.nodeService = nodeService;
        this.walletService = walletService;
        // this.utxoService = utxoService;

        // TEMP::
        blockchainRepository.deleteAllBlocks();
        transactionService.deleteAllTransactions();
        utxoService.deleteAllUTXO();

        // Add genesis block if the blockchain is empty
        if(getAllBlocks().isEmpty()){
            addGenesisBlock();
        }
    }

    public void registerEmitter(SseEmitter emitter) {
        if(emitter != null){
            this.emitters.add(emitter);
        }
    }

    public void sendLog(String logMessage) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .data(logMessage));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }

    public void sendPerformance(long hashesPerSecond) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("performance")
                        .data(hashesPerSecond));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }

    public void sendRewardEarned(long reward) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("mining-reward")
                        .data(reward));
            } catch (IOException e) {
                emitter.completeWithError(e);
                emitters.remove(emitter);
            }
        }
    }

    public List<Transaction> getWalletTransactions(String walletPublicKey){
        return blockchainRepository.getWalletTransactions(walletPublicKey);
    }


    public MiningBlockContainer getBlockToMine(String minerPublicKey) {
        if (nodeService.getMemPool().isEmpty()) {
            System.out.println("[MINING] Not enough transactions in mempool to mine a block");
            sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Not enough transactions in mempool to mine a block");
            throw new ApiException("Not enough transactions in mempool to mine a block", 400);
        }
        // Choose transactions from mempool
        sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Selecting transactions from mempool for mining");
        long blockSize = 0;
        long totalFee = 0;
        List<Transaction> transactions = new ArrayList<>();
        while (!nodeService.getMemPool().isEmpty() && transactions.size() < Block.MAX_BLOCK_SIZE_TRANSACTIONS) {
            Transaction transaction = nodeService.getMemPool().peek();
            if (blockSize + transaction.getSizeInBytes() > Block.MAX_BLOCK_SIZE_BYTES) {
                break;
            }
            if (transactionService.isTransactionValid(transaction)) {
                transaction.setStatus(TransactionStatus.CONFIRMED);
                transactions.add(transaction);
                nodeService.removeTransactionFromMemPool(transaction);
                totalFee += transaction.getTransactionFee();
                blockSize += transaction.getSizeInBytes();
            }
            else {
                System.out.println("[MINING] Invalid transaction found in mempool: " + transaction.getTransactionId());
                //sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Invalid transaction found in mempool: " + transaction.getTransactionId());
                nodeService.removeTransactionFromMemPool(transaction);
                transactionService.changeTransactionStatus(transaction.getTransactionId(), TransactionStatus.REJECTED);
                throw new ApiException("Invalid transaction found in mempool: " + transaction.getTransactionId(), 400);
            }
        }
        // After transactions are chosen, create a transaction with fee and mining reward for the miner
        long miningRewardAmount = getCurrentMiningReward();
        sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Setting up mining reward and fees for the miner");
        Transaction rewardTransaction = new Transaction(
                new ArrayList<>(),
                null,
                Instant.now().toEpochMilli(),
                miningRewardAmount + totalFee,
                "",
                minerPublicKey,
                TransactionStatus.CONFIRMED,
                0
        );
        UTXO miningReward = new UTXO(
                rewardTransaction.getTransactionId(),
                0,
                minerPublicKey,
                miningRewardAmount
        );
        UTXO fees = new UTXO(
                rewardTransaction.getTransactionId(),
                1,
                minerPublicKey,
                totalFee
        );
        rewardTransaction.setOutputs(Arrays.asList(miningReward, fees));
        rewardTransaction.recalculateTransactionId();
        transactions.add(rewardTransaction);

        Block blockToMine = new Block(
                blockchainRepository.getLatestBlockIndex() + 1,
                blockchainRepository.getLatestBlockHash(),
                transactions,
                Instant.now().toEpochMilli(),
                0,
                null
        );
        //System.out.println("[MINING] Block to mine: " + blockToMine.getIndex() + ", previous hash: " + blockToMine.getPreviousHash());
        return new MiningBlockContainer(blockToMine, getBlockTarget(blockToMine));
    }

    /*public void mineBlock(String minerPublicKey) {
        Runnable mineBlockTask = () -> {
            if (nodeService.getMemPool().isEmpty()) {
                System.out.println("[MINING] Not enough transactions in mempool to mine a block");
                sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Not enough transactions in mempool to mine a block");
                throw new ApiException("Not enough transactions in mempool to mine a block", 400);
            }
            // Choose transactions from mempool
            sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Selecting transactions from mempool for mining");
            int blockSize = 0;
            long totalFee = 0;
            List<Transaction> transactions = new ArrayList<>();
            while (!nodeService.getMemPool().isEmpty() && transactions.size() < Block.MAX_BLOCK_SIZE_TRANSACTIONS) {
                Transaction transaction = nodeService.getMemPool().peek();
                if (blockSize + transaction.getSizeInBytes() > Block.MAX_BLOCK_SIZE_BYTES) {
                    break;
                }
                if (transactionService.isTransactionValid(transaction)) {
                    transaction.setStatus(TransactionStatus.CONFIRMED);
                    transactions.add(transaction);
                    nodeService.removeTransactionFromMemPool(transaction);
                    totalFee += transaction.getTransactionFee();
                    blockSize += transaction.getSizeInBytes();
                } else {
                    System.out.println("[MINING] Invalid transaction found in mempool: " + transaction.getTransactionId());
                    sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Invalid transaction found in mempool: " + transaction.getTransactionId());
                    throw new ApiException("Invalid transaction found in mempool: " + transaction.getTransactionId(), 400);
                }
            }
            // After transactions are chosen, create a transaction with fee and mining reward for the miner
            long miningRewardAmount = getCurrentMiningReward();
            sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Setting up mining reward and fees for the miner");
            Transaction rewardTransaction = new Transaction(
                    new ArrayList<>(),
                    null,
                    Instant.now().toEpochMilli(),
                    miningRewardAmount + totalFee,
                    null,
                    minerPublicKey,
                    TransactionStatus.CONFIRMED,
                    0
            );
            UTXO miningReward = new UTXO(
                    rewardTransaction.getTransactionId(),
                    0,
                    minerPublicKey,
                    miningRewardAmount
            );
            UTXO fees = new UTXO(
                    rewardTransaction.getTransactionId(),
                    1,
                    minerPublicKey,
                    totalFee
            );
            rewardTransaction.setOutputs(Arrays.asList(miningReward, fees));
            rewardTransaction.recalculateTransactionId();
            transactions.add(rewardTransaction);

            Block minedBlock = new Block(
                    blockchainRepository.getLatestBlockIndex() + 1,
                    blockchainRepository.getLatestBlockHash(),
                    transactions,
                    Instant.now().toEpochMilli(),
                    0,
                    null
            );
            minedBlock.setBlockHash(minedBlock.calculateHash());

            sendRewardEarned(miningRewardAmount + totalFee);
            sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Starting to mine a new block...");

            int totalHashes = 0;
            Instant startTime = Instant.now();
            while (!minedBlock.checkProofOfWork()) {
                minedBlock.incrementNonce();
                minedBlock.setBlockHash(minedBlock.calculateHash());
                totalHashes++;
                System.out.println("[MINING] Trying to mine block: " + minedBlock.getBlockHash() + " with nonce: " + minedBlock.getNonce());
                //sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Trying to mine block: " + minedBlock.getBlockHash() + " with nonce: " + minedBlock.getNonce());
            }
            Instant endTime = Instant.now();
            sendPerformance(totalHashes / (endTime.toEpochMilli() - startTime.toEpochMilli()) * 1000);

            minedBlock.setBlockHash(minedBlock.calculateHash());
            System.out.println("[MINING] Mined a new block: " + minedBlock.getBlockHash() + " with " + transactions.size() + " transactions");
            sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Mined a new block: " + minedBlock.getBlockHash() + " with " + transactions.size() + " transactions");
            //transactions.forEach(System.out::println);

            if (nodeService.sendBlock(minedBlock)) {
                System.out.println("[MINING] Block was accepted by peers: " + minedBlock.getBlockHash());
                sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Block was accepted by peers: " + minedBlock.getBlockHash());
                if (!addBlock(minedBlock)) {
                    System.out.println("[MINING] Error while adding the mined block to blockchain");
                    sendLog("[" + LocalDateTime.now().format(TIME_FORMATTER) + "] Error while adding the mined block to blockchain");
                }
            }
        };
        Future result = nodeService.getExecutor().submit(mineBlockTask);
        try {
            result.get();
        } catch (Exception e) {
            try {
                throw e;
            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }*/

    public long getCurrentMiningReward(){
        return (INITIAL_REWARD_COINS / (2L * max(1, (int)(blockchainRepository.getLatestBlockIndex() / HALVING_STEP_BLOCKS)))) * 100_000_000L;
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
                break;
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
        CountDownLatch latch = new CountDownLatch(nodeService.getPeers().size());
        for(String peer : nodeService.getPeers()){
            Runnable discoverNewBlocksFromPeer = () -> {
                Set<Block> blocks = new HashSet<>();
                boolean isChainValid = true;
                try {
                    String url = peer + "/api/blocks/latest";
                    Block latestBlock = nodeService.getRestTemplate().getForObject(url, Block.class);
                    if(latestBlock != null && latestBlock.getIndex() > latestBlockIndex) {
                        blocks.add(latestBlock);
                        for(long i = latestBlock.getIndex() - 1; i > latestBlockIndex; --i) {
                            url = peer + "/api/blocks/" + latestBlock.getPreviousHash();
                            latestBlock = nodeService.getRestTemplate().getForObject(url, Block.class);
                            if(latestBlock != null && latestBlock.getIndex() == i) {
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
                            System.out.println("[BLOCK SEARCH] Found " + blocks.size() + " new blocks from " + peer);
                        }
                        else{
                            System.out.println("[BLOCK SEARCH] Invalid chain from " + peer);
                        }
                    }
                }
                catch (Exception e) {
                    //System.out.println("[BLOCK SEARCH] Error: " + peer);
                    e.printStackTrace();
                }
                latch.countDown();
            };
            nodeService.execute(discoverNewBlocksFromPeer);
        }

        try{
            latch.await();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }

        return blockLists;
    }

    public List<SseEmitter> getEmitters() {
        return emitters;
    }

    public void addGenesisBlock(){
        final String GENESIS_PUBLIC_KEY = "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAESChDsZKlIMTGd6vj/rYasBGlAMYUrmTNhCixTnTbz4ObttEIRwl7AfFA8z+jMbutq3ksC4KRvHEEfA7RbYis+w==";
        Transaction firstTransaction = new Transaction(
                new ArrayList<>(),
                null,
                BlockchainService.REFERENCE_TIMESTAMP,
                100 * 100_000_000L,
                "",
                GENESIS_PUBLIC_KEY,
                TransactionStatus.CONFIRMED,
                0
        );
        firstTransaction.setDigitalSignature(new byte[1]);
        UTXO firstUTXO = new UTXO(
                firstTransaction.getTransactionId(),
                0,
                GENESIS_PUBLIC_KEY,
                100 * 100_000_000L
        );
        firstTransaction.setOutputs(Arrays.asList(firstUTXO));

        Block genesisBlock = new Block(
                BlockchainService.REFERENCE_HEIGHT,
                "GENESIS",
                Arrays.asList(firstTransaction),
                BlockchainService.REFERENCE_TIMESTAMP,
                0,
                null
        );
        genesisBlock.setBlockHash(getStringHash(genesisBlock));

        utxoService.addUTXO(firstUTXO);
        blockchainRepository.saveBlock(genesisBlock);

        Map<String, String> wallets = walletService.getWallets();
        if(genesisBlock.getTransactions().stream().map(Transaction::getSenderPublicKey).anyMatch(wallets::containsKey) ||
           genesisBlock.getTransactions().stream().map(Transaction::getReceiverPublicKey).anyMatch(wallets::containsKey)){
            transactionRepository.saveTransaction(firstTransaction);
        }
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
        System.out.println("PROOF OF WORK CHECK:" + checkProofOfWork(block));
        System.out.println("BLOCK VALIDITY CHECK:" + isValid(block));
        if (blockchainRepository.getBlock(block.getBlockHash()) != null){
            System.out.println("[BLOCK NOT ADDED] Block already exists: " + block.getBlockHash());
            return false;
        }
        if(!isValid(block)) {
            System.out.println("[BLOCK NOT ADDED] Block is not valid: " + block.getBlockHash());
            return false;
        }
        boolean isRewardTransactionFound = false;
        for(int i = 0; i < block.getTransactions().size(); i++) {
            Transaction transaction = block.getTransactions().get(i);
            if(!transactionService.isTransactionValid(transaction)) {
                if(!isRewardTransactionFound){
                    if(!transactionService.isRewardTransactionValid(block, transaction)) {
                        System.out.println("[BLOCK NOT ADDED] Invalid reward transaction found in block: " + transaction.getTransactionId());
                        return false;
                    }
                    isRewardTransactionFound = true;
                }
                else {
                    System.out.println("[BLOCK NOT ADDED] Invalid transaction found in block: " + transaction.getTransactionId());
                    return false;
                }
            }
        }
        if (blockchainRepository.saveBlock(block)) {
            System.out.println("[BLOCK ADDED] " + block.getBlockHash());
            if (nodeService.getUnlinkedBlocks().containsKey(block.getBlockHash())){
                // recursively add all unlinked blocks
                addBlock(nodeService.getUnlinkedBlocks().get(block.getBlockHash()));
            }
            nodeService.sendBlock(block);

            // after block is saved, check for our node's pending transactions and mempool
            // if found, change status to "confirmed", delete inputs and add outputs to UTXO db

            // TEMP:: for testing purpose
            /*block.getTransactions().add(new Transaction(
                    new ArrayList<>(),
                    List.of(new UTXO(
                            "",
                            0,
                            "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEF5IaBI2wOgT6/i5GCe6pmOx2WqeOELy3Xf7xAtztp6i/AqQMRtnawbJCPkzvclm3OQEUEqfjl1FRT6g6srHBbQ==",
                            100_000_000_000L
                    )),
                    Instant.now().toEpochMilli(),
                    100_000_000_000L,
                    "",
                    "MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEF5IaBI2wOgT6/i5GCe6pmOx2WqeOELy3Xf7xAtztp6i/AqQMRtnawbJCPkzvclm3OQEUEqfjl1FRT6g6srHBbQ==",
                    TransactionStatus.CONFIRMED,
                    0
            ));*/
            // ::
            for(Transaction transaction : block.getTransactions()) {
                if(nodeService.memPoolContains(transaction)){
                    nodeService.removeTransactionFromMemPool(transaction);
                }
                if(transactionRepository.getTransaction(transaction.getTransactionId()) != null) {
                    transactionService.changeTransactionStatus(transaction.getTransactionId(), TransactionStatus.CONFIRMED);
                }
                if(isTransactionOwnedByUserWallets(transaction) && transactionService.getTransaction(transaction.getTransactionId()) == null) {
                    transactionService.saveTransaction(transaction);
                }
                for(String input : transaction.getInputs()) {
                    String[] keyParts = input.split(":");
                    utxoService.deleteUTXO(keyParts[0], keyParts[1], Integer.parseInt(keyParts[2]));
                    nodeService.unlockUTXO(input);
                }
                for(UTXO output : transaction.getOutputs()) {
                    utxoService.addUTXO(output);
                }
            }
            return true;
        }
        else {
            System.out.println("[BLOCK NOT ADDED] Failed to save block: " + block.getBlockHash());
            return false;
        }
    }

    public BigInteger getBlockTarget(Block block){
        int halfLifeTime = BlockchainService.BLOCK_TARGET_TIME * 3; // time in seconds to be ahead of schedule to halve the difficulty
        long tActual = block.getTimeStamp() - REFERENCE_TIMESTAMP; // time in seconds since the reference timestamp
        long tIdeal = (block.getIndex() - BlockchainService.REFERENCE_HEIGHT) * BlockchainService.BLOCK_TARGET_TIME;
        System.out.println("tActual: " + tActual + ", tIdeal: " + tIdeal + ", halfLifeTime: " + halfLifeTime);
        System.out.println("tActual - tIdeal: " + (tActual - tIdeal));
        System.out.println(Math.pow(2, Math.max(-64, Math.min(64, (double)(tActual - tIdeal) / halfLifeTime))));
        double exponent = Math.max(-64, Math.min(64, (double)(tActual - tIdeal) / halfLifeTime));
        System.out.println("Exponent: " + exponent);
        BigDecimal targetDecimal = new BigDecimal(BlockchainService.TARGET_REF)
                .multiply(BigDecimal.valueOf(Math.pow(2, exponent)), MathContext.DECIMAL128);
        BigInteger target = targetDecimal.toBigInteger();
        System.out.println("Block difficulty: " + target);
        return target;
    }

    private boolean isTransactionOwnedByUserWallets(Transaction transaction) {
        for (String walletPublicKey : walletService.getWalletList().stream().map(Wallet::getPublicKey).toList()) {
            if (transaction != null &&
                    ((transaction.getSenderPublicKey() != null && transaction.getSenderPublicKey().equals(walletPublicKey)) ||
                    (transaction.getReceiverPublicKey() != null && transaction.getReceiverPublicKey().equals(walletPublicKey)))) {
                return true;
            }
        }
        return false;
    }

    public byte[] getBlockHash(Block block) {
        String stringToHash = block.getIndex()
                + block.getPreviousHash()
                + block.getTimeStamp()
                + block.getNonce()
                + block.getTransactions().toString();
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(stringToHash.getBytes(StandardCharsets.UTF_8));
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getStringHash(Block block){
        byte[] hash = getBlockHash(block);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            // 0xff = 11111111
            // 0xff & b for b to be treated as an unsigned 8-bit value
            String hex = Integer.toHexString(0xff & b);
            // 2 digits, if 1 then append 0
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private boolean checkProofOfWork(Block block){
        if(!block.getBlockHash().equals(getStringHash(block))){
            System.out.println("[BLOCK NOT VALID] Block hash didn't match (" + block.getBlockHash() + ")");
            return false;
        }
        BigInteger hashInt = new BigInteger(1, getBlockHash(block));
        BigInteger target = getBlockTarget(block);
        if(hashInt.compareTo(target) >= 0){
            System.out.println("[BLOCK NOT VALID] Block hash didn't pass proof of work test with target " + target);
            return false;
        }
        return true;
    }

    private boolean isValid(Block block){
        if(!checkProofOfWork(block)){
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
            for(Iterator<Block> descItr = previousBlocks.descendingIterator(); descItr.hasNext();) {
                Block blockToAdd = (Block)descItr.next();
                addBlock(blockToAdd);
            }
        }

        previousBlock = blockchainRepository.getBlock(block.getPreviousHash());

        // !! For testing previousBlock.calculateHash() >> previousBlock.getBlockHash() as previousBlock is hardcoded
        if(!Objects.equals(getStringHash(previousBlock), block.getPreviousHash())){
            System.out.println("[BLOCK NOT VALID] Previous hash didn't match (\n\t" + getStringHash(block) + "\n\t" + block.getPreviousHash() + "\n)");
            System.out.println(previousBlock);
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

    public Map<Long, Block> getBlocks(long from, long count) {
        Map<Long, Block> blocks = new HashMap<>();
        long startWith = from;
        /*if(from > blockchainRepository.getLatestBlockIndex() || from < 0) {
            startWith = blockchainRepository.getLatestBlockIndex();
        }*/
        Block block = blockchainRepository.getBlock(from > blockchainRepository.getLatestBlockIndex() || from < 0 ? blockchainRepository.getLatestBlockIndex() : startWith - 1);
        for(long i = startWith - 1; block != null && i > startWith - count; --i) {
            System.out.println("[BLOCK] " + block.getIndex() + " " + block.getBlockHash() + " " + LocalDateTime.ofEpochSecond(block.getTimeStamp() / 1000, 0, ZoneOffset.UTC).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            blocks.put(block.getIndex(), block);
            block = blockchainRepository.getBlock(block.getPreviousHash());
        }
        //System.out.println("a-------");
        return blocks;
    }

    public long getBlockchainSizeInBytes() {
        long size = 0;
        for (Block block : getAllBlocks().values()) {
            size += block != null ? block.getSizeInBytes() : 0;
        }
        return size;
    }

    public long getTransactionCount() {
        long count = 0;
        for (Block block : getAllBlocks().values()) {
            count += block.getTransactions().size();
        }
        return count;
    }

    public boolean isBlockchainSync() {
        CountDownLatch latch = new CountDownLatch(nodeService.getPeers().size());
        String latestBlockHash = blockchainRepository.getLatestBlockHash();
        Map<String, Integer> latestBlocks = new ConcurrentHashMap<>();
        for(String peer : nodeService.getPeers()){
            Runnable getLatestBlock = () -> {
                try {
                    String url = peer + "/api/blocks/latest";
                    Block latestBlock = nodeService.getRestTemplate().getForObject(url, Block.class);
                    if(latestBlock != null) {
                        if(latestBlocks.containsKey(latestBlock.getBlockHash())){
                            latestBlocks.replace(latestBlock.getBlockHash(), latestBlocks.get(latestBlock.getBlockHash()) + 1);
                        }
                        else {
                            latestBlocks.put(latestBlock.getBlockHash(), 1);
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                latch.countDown();
            };
            nodeService.execute(getLatestBlock);
        }

        try{
            latch.await();
        }
        catch (InterruptedException e){
            e.printStackTrace();
        }
        String mostCommonBlockHash = latestBlocks.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if(mostCommonBlockHash == null) {
            System.out.println("[BLOCKCHAIN SYNC] No latest block found from peers");
            return true;
            //throw new ApiException("No latest block found from peers", 404);
        }

        return mostCommonBlockHash.equals(latestBlockHash);
    }

    public Map<Long, Block> getBlocksFrom(long from) {
        Map<Long, Block> blocks = new HashMap<>();
        for(long i = from + 1; i <= blockchainRepository.getLatestBlockIndex(); ++i) {
            Block block = blockchainRepository.getBlock(i);
            if(block != null) {
                blocks.put(block.getIndex(), block);
            }
        }
        return blocks;
    }
}
