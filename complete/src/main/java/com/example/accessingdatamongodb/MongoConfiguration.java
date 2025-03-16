package com.example.accessingdatamongodb;

import com.mongodb.AutoEncryptionSettings;
import com.mongodb.ClientEncryptionSettings;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.CreateEncryptedCollectionParams;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.vault.ClientEncryption;
import com.mongodb.client.vault.ClientEncryptions;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@Configuration
public class MongoConfiguration {

    @Bean
    public MongoClientSettings mongoClientSettings() {
        var clientSettingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString("mongodb://localhost,localhost:27018"));

        var kmsProviders = getKmsProviders();

        BsonBinary dataKeyId;

        try (var client = MongoClients.create(clientSettingsBuilder.build())) {
            try (var clientEncryption = ClientEncryptions.create(
                    ClientEncryptionSettings.builder()
                            .keyVaultMongoClientSettings(clientSettingsBuilder.build())
                            .keyVaultNamespace("encryption.__keyVault")
                            .kmsProviders(kmsProviders)
                            .build())) {
                dataKeyId = clientEncryption.createDataKey("local", new DataKeyOptions());
                // QE
                createEncryptedCollection(client, clientEncryption);
            }
        }

        var autoEncryptionSettings = AutoEncryptionSettings.builder()
                .keyVaultNamespace("encryption.__keyVault")
                .kmsProviders(kmsProviders)
                .extraOptions(createExtraOptions())
                // CSFLE
//                .schemaMap(Map.of("test.customer", getSchemaDocument(dataKeyId)))
                // QE
                .encryptedFieldsMap(Map.of("test.customer", getEncryptedFieldsMap(dataKeyId)))
                .build();

        return clientSettingsBuilder
                .autoEncryptionSettings(autoEncryptionSettings)
                .build();
    }

    private static Map<String, Map<String, Object>> getKmsProviders() {
        var localMasterKey = new byte[96];
        new SecureRandom().nextBytes(localMasterKey);

        return Map.of("local", Map.of("key", localMasterKey));
    }

    private static Map<String, Object> createExtraOptions() {
        var mongodbCryptSharedLibPath = System.getenv("MONGODB_CRYPT_SHARED_LIB_PATH");
        return mongodbCryptSharedLibPath == null
                ? Map.of() : Map.of("cryptSharedLibPath", mongodbCryptSharedLibPath);
    }

    // QE
    private static void createEncryptedCollection(MongoClient client, ClientEncryption clientEncryption) {
        var encryptedFieldsMap = getEncryptedFieldsMapForCollectionCreation();
        var createCollectionOptions = new CreateCollectionOptions().encryptedFields(encryptedFieldsMap);
        var encryptedCollectionParams = new CreateEncryptedCollectionParams("local")
                .masterKey(new BsonDocument());
        var database = client.getDatabase("test");
        database.getCollection("customer").drop();
        clientEncryption.createEncryptedCollection(database, "customer",
                createCollectionOptions, encryptedCollectionParams);
    }

    // QE
    private static BsonDocument getEncryptedFieldsMap(BsonBinary dataKeyId) {
        var encryptedFieldsMap = getEncryptedFieldsMapForCollectionCreation();
        encryptedFieldsMap.getArray("fields").get(0).asDocument().put("keyId", dataKeyId);
        return encryptedFieldsMap;
    }

    private static BsonDocument getEncryptedFieldsMapForCollectionCreation() {
        return BsonDocument.parse("""
                {
                    "fields": [
                        {
                            "keyId": null,
                            "path": "socialSecurityNumber",
                            "bsonType": "string",
                            "queries": [{"queryType": "equality"}]
                        }
                    ]
                }""");
    }

    // CSFLE
    private static BsonDocument getSchemaDocument(BsonBinary dataKeyId) {
        var schemaDocument = BsonDocument.parse("""
                {
                    "bsonType": "object",
                    "properties": {
                        "socialSecurityNumber": {
                            "encrypt": {
                                "bsonType": "string",
                                "algorithm": "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic",
                            }
                        },
                    },
                }
                """);
        schemaDocument.append("encryptMetadata", new BsonDocument().append("keyId", new BsonArray(List.of(dataKeyId))));
        return schemaDocument;
    }
}