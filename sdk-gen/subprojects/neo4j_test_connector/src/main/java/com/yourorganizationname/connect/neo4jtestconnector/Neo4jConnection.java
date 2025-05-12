package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

import java.util.Properties;

public class Neo4jConnection {
    private ConnectionProperties connectionProperties;
    private final String dbHost;
    private final String dbPort;
    private String dbUser;
    private String dbPassword;
    private final boolean dbSSL;

    public Neo4jConnection(ConnectionProperties connectionProperties) {
        this.connectionProperties = connectionProperties;
        dbHost = (String) connectionProperties.get("host");
        dbPort = (String) connectionProperties.get("port");
        dbUser = (String) connectionProperties.get("username");
        dbPassword = (String) connectionProperties.get("password");
        Object dbSSLObject = connectionProperties.get("ssl");
        dbSSL = dbSSLObject != null && Boolean.parseBoolean(dbSSLObject.toString());
    }

    public Neo4jConnection(Properties properties) {
        dbHost = properties.getProperty("host");
        dbPort = properties.getProperty("port");
        dbUser = properties.getProperty("username");
        dbPassword = properties.getProperty("password");
        Object dbSSLObject = properties.get("ssl");
        dbSSL = dbSSLObject != null && Boolean.parseBoolean(dbSSLObject.toString());
    }

    public Driver getDriver() {
        return GraphDatabase.driver(getDbUri(), getAuthToken());
    }

    public String getDbUri() {
        final String protocol = dbSSL ? "bolt+s" : "bolt";
        return protocol + "://" + dbHost + ":" + dbPort;
    }

    public AuthToken getAuthToken() {
        if (dbUser != null && dbPassword != null) {
            return AuthTokens.basic(dbUser, dbPassword);
        } else {
            throw new IllegalArgumentException("Username and password must be provided");
        }
    }

    public ConnectionProperties getConnectionProperties() {
        return connectionProperties;
    }
}
