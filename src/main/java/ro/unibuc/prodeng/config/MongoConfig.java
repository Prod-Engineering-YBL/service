package ro.unibuc.prodeng.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

import java.util.Collection;
import java.util.Collections;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${mongodb.connection.url}")
    private String connectionURL;

    @Override
    protected @NonNull String getDatabaseName() {
        return "test";
    }

    @Override
    @SuppressWarnings("null")
    public @NonNull MongoClient mongoClient() {
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionURL))
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Override
    @SuppressWarnings("null")
    public @NonNull Collection<String> getMappingBasePackages() {
        return Collections.singleton("ro.unibuc.prodeng.model");
    }
}
