package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;
import com.ibm.wdp.connect.common.sdk.api.models.DiscoveredAssetType;
import com.google.gson.Gson;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.*;

public class Neo4jResourceMapper {

    public static final String LABEL_QUERY = "CALL db.labels();";
    public static final String RELATIONSHIP_QUERY = "CALL db.relationshipTypes();";

    public static final String LABEL_PARAM_QUERY = "MATCH (n:<TYPE>)\n" +
            "WITH DISTINCT keys(n) AS properties\n" +
            "UNWIND properties AS property\n" +
            "RETURN DISTINCT property\n" +
            "ORDER BY property;";

    public static final String LABEL_PARAM_QUERY_PROPERTIES = "MATCH (n:<TYPE>)\n" +
            "RETURN properties(n) AS properties, elementId(n) as neo4j_internal_elementId;";

    public static final String RELATIONSHIP_PARAM_QUERY = "MATCH (s)-[r:<TYPE>]->(t)\n" +
            "WITH DISTINCT keys(r) AS properties\n" +
            "UNWIND properties AS property\n" +
            "RETURN DISTINCT property\n" +
            "ORDER BY property;";

    public static final String RELATIONSHIP_PARAM_QUERY_PROPERTIES = "MATCH (s)-[r:<TYPE>]->(t)\n" +
            "RETURN properties(r) as properties, elementId(r) as neo4j_internal_elementId, " +
            "elementId(s) as neo4j_internal_sourceElementId, elementId(t) as neo4j_internal_targetElementId";

    private Neo4jConnection connection = null;

    public void connect(Properties connectionProperties) throws Exception {
        this.connection = new Neo4jConnection(connectionProperties);
        try (Driver driver = connection.getDriver()) {
            driver.verifyConnectivity();
        } catch (Exception e) {
            throw new Exception("Failed to connect to Neo4j database: " + e.getMessage(), e);
        }

    }

    public List<CustomFlightAssetDescriptor> getAssetDescriptors(ConnectionProperties connectionProperties, String path) {
        List<CustomFlightAssetDescriptor> response = new ArrayList<>();
        final Neo4jConnection connection = new Neo4jConnection(connectionProperties);
        Neo4jPath neo4jPath = new Neo4jPath(path);

        try (Driver driver = connection.getDriver()) {
            driver.verifyConnectivity();
            try (Session session = driver.session()) {
                if (neo4jPath.isGetAll()) {
                    System.out.println("GET_ALL_ASSET_DESCRIPTORS");
                    response = this.getTypeDescriptors();
                } else if (neo4jPath.isGetType()) {
                    System.out.println("GET_TYPE_DESCRIPTOR");
                    response = this.getAllAssetDescriptorsForType(session, neo4jPath.getType());
                } else {
                    System.out.println("GET_ASSET_DESCRIPTOR_FOR_PATH: " + path);
                    response = this.getAssetDescriptorForPath(session, neo4jPath);
                }
            }
        }
        return response;
    }

    public List<CustomFlightAssetDescriptor> getTypeDescriptors() {
        List<CustomFlightAssetDescriptor> response = new ArrayList<>();
        response.add(this.getTypeAssetDescriptor("Label"));
        response.add(this.getTypeAssetDescriptor("Relationship"));
        return response;
    }

    public List<CustomFlightAssetDescriptor> getAllAssetDescriptorsForType(Session session, String type) {
        List<CustomFlightAssetDescriptor> response = new ArrayList<>();
        if (type.equals("Label")) {
            Result labels = session.run(LABEL_QUERY);
            while (labels.hasNext()) {
                Record label = labels.next();
                response.add(this.getFlightAssetDescriptor(label.get(0).asString(), "Label"));
            }
        }
        if (type.equals("Relationship")) {
            Result relationships = session.run(RELATIONSHIP_QUERY);
            while (relationships.hasNext()) {
                Record relationship = relationships.next();
                response.add(this.getFlightAssetDescriptor(relationship.get(0).asString(), "Relationship"));
            }
        }
        return response;
    }

    public List<CustomFlightAssetDescriptor> getAssetDescriptorForPath(Session session, Neo4jPath path) {
        List<CustomFlightAssetDescriptor> response = new ArrayList<>();
        String type = path.getType();
        String name = path.getName();
        // TODO: Escape type to prevent injection
        List<CustomFlightAssetField> fields = this.getFieldsForPath(session, path);
        response.add(this.getFlightAssetDescriptor(name, type, fields));
        return response;
    }

    public List<CustomFlightAssetField> getFields(ConnectionProperties connectionProperties, Neo4jPath path) {
        this.connection = new Neo4jConnection(connectionProperties);
        try (Driver driver = connection.getDriver()) {
            driver.verifyConnectivity();
            try (Session session = driver.session()) {
                return this.getFieldsForPath(session, path);
            }
        }
    }

    public List<String> getJSONRecordsForPath(ConnectionProperties connectionProperties, Neo4jPath path) {
        this.connection = new Neo4jConnection(connectionProperties);
        try (Driver driver = connection.getDriver()) {
            driver.verifyConnectivity();
            try (Session session = driver.session()) {
                String query = getQueryForType(path.getType(), true).replace("<TYPE>", path.getName());
                Result properties = session.run(query);
                List<String> records = new ArrayList<>();
                Gson gson = new Gson();
                while (properties.hasNext()) {
                    Record property = properties.next();
                    Map<String, Object> propMap = property.get(0).asMap();
                    String record = gson.toJson(propMap);
                    records.add(record);
                }
                return records;
            }
        }
    }

    public List<Map<String, Object>> getValuesForPath(ConnectionProperties connectionProperties, Neo4jPath path) {
        this.connection = new Neo4jConnection(connectionProperties);
        try (Driver driver = connection.getDriver()) {
            driver.verifyConnectivity();
            try (Session session = driver.session()) {
                String query = getQueryForType(path.getType(), true).replace("<TYPE>", path.getName());
                Result properties = session.run(query);
                List<Map<String, Object>> records = new ArrayList<>();
                while (properties.hasNext()) {
                    Record property = properties.next();
                    Map<String, Object> props = new HashMap<>(property.get("properties").asMap());

                    props.put("neo4j_internal_elementId", property.get("neo4j_internal_elementId").asString());
                    props.put("neo4j_internal_sourceElementId", property.get("neo4j_internal_sourceElementId").asString());
                    props.put("neo4j_internal_targetElementId", property.get("neo4j_internal_targetElementId").asString());

                    records.add(props);
                }
                return records;
            }
        }
    }

    private List<CustomFlightAssetField> getFieldsForPath(Session session, Neo4jPath path) {
        String query = getQueryForType(path.getType(), false).replace("<TYPE>", path.getName());
        Result properties = session.run(query);
        List<CustomFlightAssetField> fields = this.getIdFieldsForType(path.getType());
        while (properties.hasNext()) {
            Record property = properties.next();
            String fieldName = property.get(0).asString();
            CustomFlightAssetField field = new CustomFlightAssetField();
            field.setName(fieldName);
            field.setType("String");
            fields.add(field);
        }
        return fields;
    }

    private String getQueryForType(String type, Boolean properties) {
        if (type.equals("Label")) {
            if (properties) {
                return LABEL_PARAM_QUERY_PROPERTIES;
            }
            return LABEL_PARAM_QUERY;
        } else if (type.equals("Relationship")) {
            if (properties){
                return RELATIONSHIP_PARAM_QUERY_PROPERTIES;
            }
            return RELATIONSHIP_PARAM_QUERY;
        }
        throw new IllegalArgumentException("Invalid type: " + type);
    }

    private CustomFlightAssetDescriptor getFlightAssetDescriptor(String name, String type){
        return this.getFlightAssetDescriptor(name, type, new ArrayList<>());
    }

    private CustomFlightAssetDescriptor getTypeAssetDescriptor(String type){
        CustomFlightAssetDescriptor descriptor = new CustomFlightAssetDescriptor();
        descriptor.setName(type);
        descriptor.setPath("/" + type);
        DiscoveredAssetType assetType = new DiscoveredAssetType();
        assetType.setType(type);
        assetType.setDataset(false);
        assetType.setDatasetContainer(true);
        descriptor.setAssetType(assetType);
        return descriptor;
    }

    private CustomFlightAssetDescriptor getFlightAssetDescriptor(String name, String type, List<CustomFlightAssetField> fields) {
        CustomFlightAssetDescriptor descriptor = new CustomFlightAssetDescriptor();
        descriptor.setName(name);
        descriptor.setPath("/" + type + "/" + name);
        DiscoveredAssetType assetType = new DiscoveredAssetType();
        assetType.setType(type);
        assetType.setDataset(true);
        assetType.setDatasetContainer(false);
        descriptor.setAssetType(assetType);

        descriptor.setFields(fields);
        return descriptor;
    }

    private List<CustomFlightAssetField> getIdFieldsForType(String type){
        List<CustomFlightAssetField> fields = new ArrayList<>();
        if (type.equals("Label")) {
            CustomFlightAssetField field = new CustomFlightAssetField();
            field.setName("neo4j_internal_elementId");
            field.setType("String");
            fields.add(field);
        } else if (type.equals("Relationship")) {
            CustomFlightAssetField elementIdField = new CustomFlightAssetField();
            elementIdField.setName("neo4j_internal_elementId");
            elementIdField.setType("String");
            fields.add(elementIdField);
            CustomFlightAssetField sourceNodeElementIdField = new CustomFlightAssetField();
            sourceNodeElementIdField.setName("neo4j_internal_sourceElementId");
            sourceNodeElementIdField.setType("String");
            fields.add(sourceNodeElementIdField);
            CustomFlightAssetField targetNodeElementIdField = new CustomFlightAssetField();
            targetNodeElementIdField.setName("neo4j_internal_targetElementId");
            targetNodeElementIdField.setType("String");
            fields.add(targetNodeElementIdField);
        }
        return fields;
    }

}
