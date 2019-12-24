package yay.linda.mydaybackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "application.mongo")
public class MongoProperties {

    private String host;
    private int port;
    private String databaseName;
}
