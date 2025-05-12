package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.DiscoveredAssetType;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Neo4jResourceMapper {

    public static final String LABEL_QUERY = "CALL db.labels();";
    public static final String RELATIONSHIP_QUERY = "CALL db.relationshipTypes();";

    public void testConnection(Properties connectionProperties) throws Exception {
        final Neo4jConnection connection = new Neo4jConnection(connectionProperties);
        try (var driver = connection.getDriver()) {
            driver.verifyConnectivity();
        } catch (Exception e) {
            throw new Exception("Failed to connect to Neo4j database: " + e.getMessage(), e);
        }

    }

    public List<CustomFlightAssetDescriptor> getAssetDescriptors(ConnectionProperties connectionProperties) {
        List<CustomFlightAssetDescriptor> response = new ArrayList<>();
        final Neo4jConnection connection = new Neo4jConnection(connectionProperties);

        try (var driver = connection.getDriver()) {
            driver.verifyConnectivity();

            try (var session = driver.session()) {
                var labels = session.run(LABEL_QUERY);
                while (labels.hasNext()) {
                    var label = labels.next();
                    response.add(this.getFlightAssetDescriptor(label.get(0).asString(), "Label"));
                }
                var relationships = session.run(RELATIONSHIP_QUERY);
                while (relationships.hasNext()) {
                    var relationship = relationships.next();
                    response.add(this.getFlightAssetDescriptor(relationship.get(0).asString(), "Relationship"));
                }
            }
        }

        return response;
    }

    private CustomFlightAssetDescriptor getFlightAssetDescriptor(String name, String type) {
        CustomFlightAssetDescriptor descriptor = new CustomFlightAssetDescriptor();
        descriptor.setName(name);
        DiscoveredAssetType assetType = new DiscoveredAssetType();
        assetType.setType(type);
        descriptor.setAssetType(assetType);
        return descriptor;
    }
}
