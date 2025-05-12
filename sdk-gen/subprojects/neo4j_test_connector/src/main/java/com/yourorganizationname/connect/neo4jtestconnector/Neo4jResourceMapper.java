package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.DiscoveredAssetType;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

import java.util.ArrayList;
import java.util.List;

public class Neo4jResourceMapper {

    public static final String LABEL_QUERY = "CALL db.labels();";
    public static final String RELATIONSHIP_QUERY = "CALL db.relationshipTypes();";

    public List<CustomFlightAssetDescriptor> getAssetDescriptors(String dbHost, String dbPort, String dbUser, String dbPassword, boolean dbSSL) {
        List<CustomFlightAssetDescriptor> response = new ArrayList<>();
        final String protocol = dbSSL ? "bolt+s" : "bolt";
        final String dbUri = protocol + "://" + dbHost + ":" + dbPort;

        try (var driver = GraphDatabase.driver(dbUri,  AuthTokens.basic(dbUser, dbPassword))) {
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
