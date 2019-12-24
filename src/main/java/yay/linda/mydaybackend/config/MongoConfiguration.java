package yay.linda.mydaybackend.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Autowired
    private MongoProperties mongoProperties;

    @Override
    public MongoClient mongoClient() {
        return MongoClients.create(String.format("mongodb://%s:%s", mongoProperties.getHost(), mongoProperties.getPort()));
    }

    @Override
    protected String getDatabaseName() {
        return mongoProperties.getDatabaseName();
    }
}
