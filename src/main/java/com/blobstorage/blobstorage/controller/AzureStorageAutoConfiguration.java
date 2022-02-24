package com.blobstorage.blobstorage.controller;

import com.azure.spring.autoconfigure.storage.resource.AzureStorageProtocolResolver;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.spring.cloud.autoconfigure.context.AzureContextAutoConfiguration;
import com.microsoft.azure.spring.cloud.autoconfigure.storage.AzureStorageProperties;
import com.microsoft.azure.spring.cloud.autoconfigure.telemetry.TelemetryCollector;
import com.microsoft.azure.spring.cloud.context.core.api.EnvironmentProvider;
import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;
import com.microsoft.azure.spring.cloud.context.core.storage.StorageConnectionStringProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;

@Configuration
@AutoConfigureAfter(AzureContextAutoConfiguration.class)
@ConditionalOnClass({ BlobServiceClientBuilder.class, ShareServiceClientBuilder.class })
@ConditionalOnProperty(name = "spring.cloud.azure.storage.account")
@EnableConfigurationProperties(AzureStorageProperties.class)
public class AzureStorageAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(AzureStorageAutoConfiguration.class);
    private static final String STORAGE = "image";
    private static final String ACCOUNT_NAME = "https://viru1234.blob.core.windows.net/";

    @Autowired(required = false)
    private ResourceManagerProvider resourceManagerProvider;

    @PostConstruct
    public void collectTelemetry() {
        TelemetryCollector.getInstance().addService(STORAGE);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlobServiceClientBuilder blobServiceClientBuilder(AzureStorageProperties storageProperties,
                                                             EnvironmentProvider environmentProvider) {
        String connectionString;

        if (resourceManagerProvider != null) {
            String accountName = storageProperties.getAccount();

            StorageAccount storageAccount = resourceManagerProvider.getStorageAccountManager().getOrCreate(accountName);
            connectionString = StorageConnectionStringProvider.getConnectionString(storageAccount,
                    environmentProvider.getEnvironment(), storageProperties.isSecureTransfer());
        } else {
            connectionString = StorageConnectionStringProvider
                    .getConnectionString(storageProperties.getAccount(), storageProperties.getAccessKey(),
                            environmentProvider.getEnvironment());
            TelemetryCollector.getInstance().addProperty(STORAGE, ACCOUNT_NAME, storageProperties.getAccount());
        }


        return new BlobServiceClientBuilder().connectionString(connectionString);
//        try {
//            return CloudStorageAccount.parse(connectionString);
//        } catch (URISyntaxException | InvalidKeyException e) {
//            log.error("Failed to parse storage connection string" + connectionString, e);
//            throw new RuntimeException("Failed to parse storage connection string" + connectionString, e);
//        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ShareServiceClientBuilder shareServiceClientBuilder(AzureStorageProperties storageProperties,
                                                               EnvironmentProvider environmentProvider) {
        String connectionString;

        if (resourceManagerProvider != null) {
            String accountName = storageProperties.getAccount();

            StorageAccount storageAccount = resourceManagerProvider.getStorageAccountManager().getOrCreate(accountName);

            connectionString = StorageConnectionStringProvider
                    .getConnectionString(storageAccount, environmentProvider.getEnvironment(),
                            storageProperties.isSecureTransfer());
        } else {
            connectionString = StorageConnectionStringProvider
                    .getConnectionString(storageProperties.getAccount(), storageProperties.getAccessKey(),
                            environmentProvider.getEnvironment());
            TelemetryCollector.getInstance().addProperty(STORAGE, ACCOUNT_NAME, storageProperties.getAccount());
        }


        return new ShareServiceClientBuilder().connectionString(connectionString);
    }

    @Configuration
    @ConditionalOnClass(AzureStorageProtocolResolver.class)
    @Import(AzureStorageProtocolResolver.class)
    class StorageResourceConfiguration {
    }
}


