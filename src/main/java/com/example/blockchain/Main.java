package com.example.blockchain;

import com.example.blockchain.model.Block;
import com.example.blockchain.repository.BlockchainRepository;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Security;

@SpringBootApplication
@RestController
@EnableScheduling // enables auto execution of @Scheduled methods
public class Main {
	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		SpringApplication.run(Main.class, args);
	}

	@GetMapping("/")
	public String hello() {
		return "Hello World!";
	}

}
