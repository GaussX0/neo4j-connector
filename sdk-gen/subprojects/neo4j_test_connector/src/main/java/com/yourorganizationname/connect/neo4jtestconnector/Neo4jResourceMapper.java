package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;
import com.ibm.wdp.connect.common.sdk.api.models.DiscoveredAssetType;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Neo4jResourceMapper {

    public static final String LABEL_QUERY = "CALL db.labels();";
    public static final String RELATIONSHIP_QUERY = "CALL db.relationshipTypes();";

    public static final String LABEL_PARAM_QUERY = "MATCH (n:<TYPE>)\n" +
            "WITH DISTINCT keys(n) AS properties\n" +
            "UNWIND properties AS property\n" +
            "RETURN DISTINCT property;";

    public static final String RELATIONSHIP_PARAM_QUERY = "MATCH (s)-[r:<TYPE>]->(t)\n" +
            "WITH DISTINCT keys(r) AS properties\n" +
            "UNWIND properties AS property\n" +
            "RETURN DISTINCT property;";

    public void testConnection(Properties connectionProperties) throws Exception {
        final Neo4jConnection connection = new Neo4jConnection(connectionProperties);
        try (Driver driver = connection.getDriver()) {
            driver.verifyConnectivity();
        } catch (Exception e) {
            throw new Exception("Failed to connect to Neo4j database: " + e.getMessage(), e);
        }

    }

    public List<CustomFlightAssetDescriptor> getAssetDescriptors(ConnectionProperties connectionProperties, String path) {
        List<CustomFlightAssetDescriptor> response = new ArrayList<>();
        final Neo4jConnection connection = new Neo4jConnection(connectionProperties);

        // TODO: If path points to a specific asset return data only for that asset
        try (Driver driver = connection.getDriver()) {
            driver.verifyConnectivity();
            try (Session session = driver.session()) {
                if (path == null || path.isEmpty() || path.equals("/")) {
                    System.out.println("GET_ALL_ASSET_DESCRIPTORS");
                    response = this.getAllAssetDescriptors(session);
                } else {
                    System.out.println("GET_ASSET_DESCRIPTOR_FOR_PATH: " + path);
                    response = this.getAssetDescriptorForPath(session, path);
                }
            }
        }
        return response;
    }

    public List<CustomFlightAssetDescriptor> getAllAssetDescriptors(Session session) {
        List<CustomFlightAssetDescriptor> response = new ArrayList<>();
        Result labels = session.run(LABEL_QUERY);
        while (labels.hasNext()) {
            Record label = labels.next();
            response.add(this.getFlightAssetDescriptor(label.get(0).asString(), "Label"));
        }
        Result relationships = session.run(RELATIONSHIP_QUERY);
        while (relationships.hasNext()) {
            Record relationship = relationships.next();
            response.add(this.getFlightAssetDescriptor(relationship.get(0).asString(), "Relationship"));
        }
        return response;
    }

    public List<CustomFlightAssetDescriptor> getAssetDescriptorForPath(Session session, String path) {
        List<CustomFlightAssetDescriptor> response = new ArrayList<>();
        String type = path.substring(1, path.indexOf("_"));
        String name = path.substring(path.indexOf("_") + 1);
        // TODO: Escape type to prevent injection
        String query = getQueryForType(type).replace("<TYPE>", name);
        Result properties = session.run(query);
        List<CustomFlightAssetField> fields = new ArrayList<>();
        while (properties.hasNext()) {
            Record property = properties.next();
            String fieldName = property.get(0).asString();
            CustomFlightAssetField field = new CustomFlightAssetField();
            field.setName(fieldName);
            field.setType("String");
            fields.add(field);
        }
        response.add(this.getFlightAssetDescriptor(name, type, fields));
        return response;
    }

    private String getQueryForType(String type) {
        if (type.equals("Label")) {
            return LABEL_PARAM_QUERY;
        } else if (type.equals("Relationship")) {
            return RELATIONSHIP_PARAM_QUERY;
        }
        throw new IllegalArgumentException("Invalid type: " + type);
    }

    private CustomFlightAssetDescriptor getFlightAssetDescriptor(String name, String type){
        return this.getFlightAssetDescriptor(name, type, new ArrayList<>());
    }

    private CustomFlightAssetDescriptor getFlightAssetDescriptor(String name, String type, List<CustomFlightAssetField> fields) {
        CustomFlightAssetDescriptor descriptor = new CustomFlightAssetDescriptor();
        descriptor.setName(name);
        descriptor.setPath("/" + type + "_" + name);
        DiscoveredAssetType assetType = new DiscoveredAssetType();
        assetType.setType(type);
        assetType.setDataset(true);
        assetType.setDatasetContainer(false);
        descriptor.setAssetType(assetType);

        descriptor.setFields(fields);
        return descriptor;
    }
}
