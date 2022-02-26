package com.blobstorage.blobstorage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class BlobstorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlobstorageApplication.class, args);
	}

}
