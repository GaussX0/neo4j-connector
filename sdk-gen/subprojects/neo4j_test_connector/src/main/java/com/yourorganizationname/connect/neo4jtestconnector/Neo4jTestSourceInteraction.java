package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.connect.sdk.api.Record;
import com.ibm.connect.sdk.api.RowBasedSourceInteraction;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;
import org.apache.arrow.flight.Ticket;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Neo4jTestSourceInteraction extends RowBasedSourceInteraction<Neo4jTestConnector> {

    public Neo4jTestSourceInteraction(Neo4jTestConnector connector, CustomFlightAssetDescriptor asset) {
        super();
        setConnector(connector);
        setAsset(asset);
    }

    @Override
    public List<Ticket> getTickets() {
        int partitions = getAsset().getPartitionCount() != null ? getAsset().getPartitionCount() : 1;

        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < partitions; i++) {
            String ticketContent = String.format("%s|%s|%d",
                    getAsset().getInteractionProperties().get("schema_name"),
                    getAsset().getInteractionProperties().get("table_name"),
                    i);
            tickets.add(new Ticket(ticketContent.getBytes(StandardCharsets.UTF_8)));
        }
        return tickets;
    }

    @Override
    public List<CustomFlightAssetField> getFields() {
        List<CustomFlightAssetField> fields = new ArrayList<>();

        try (Neo4jConnection connection = new Neo4jConnection(getConnector().getConnectionProperties());
             Session session = connection.getDriver().session()) {

            String tableName = (String) getAsset().getInteractionProperties().get("table_name");

            if (tableName == null || tableName.isEmpty()) {
                return fields;
            }

            // Cypher to get all distinct property keys for nodes with the tableName
            String cypher = String.format(
                    "MATCH (n:`%s`) " +
                            "WITH keys(n) AS props " +
                            "UNWIND props AS prop " +
                            "RETURN DISTINCT prop",
                    tableName);

            Result result = session.run(cypher);

            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                String prop = record.get("prop").asString();

                // Sample one property value to guess its type
                String typeCypher = String.format(
                        "MATCH (n:`%s`) WHERE exists(n.%s) RETURN n.%s AS value LIMIT 1",
                        tableName, prop, prop);

                Result typeResult = session.run(typeCypher);
                Object sampleValue = null;
                if (typeResult.hasNext()) {
                    org.neo4j.driver.Record typeRecord = typeResult.next();
                    if (!typeRecord.get("value").isNull()) {
                        sampleValue = neo4jValueToJava(typeRecord.get("value"));
                    }
                }

                String fieldType = mapJavaTypeToFieldType(sampleValue);

                fields.add(new CustomFlightAssetField()
                        .name(prop)
                        .type(fieldType)
                        .nullable(true));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            // Fallback or propagate exception depending on your needs
        }

        return fields;
    }

    private String mapJavaTypeToFieldType(Object sampleValue) {
        if (sampleValue == null) {
            return "string"; // Default type if unknown
        }
        if (sampleValue instanceof Integer) {
            return "int";
        }
        if (sampleValue instanceof Long) {
            return "long";
        }
        if (sampleValue instanceof Double || sampleValue instanceof Float) {
            return "double";
        }
        if (sampleValue instanceof Boolean) {
            return "boolean";
        }
        if (sampleValue instanceof String) {
            return "string";
        }
        // add other mappings if needed
        return "string"; // Default fallback
    }

    /**
     * Converts Neo4j Value to corresponding Java object
     */
    private Object neo4jValueToJava(org.neo4j.driver.Value value) {
        if (value.isNull()) {
            return null;
        }
        switch (value.type().name()) {
            case "STRING":
                return value.asString();
            case "INTEGER":
                return value.asInt();
            case "FLOAT":
                return value.asDouble();
            case "BOOLEAN":
                return value.asBoolean();
            default:
                return value.asObject();
        }
    }

    @Override
    public Record getRecord() {
        return new Record();
    }

    @Override
    public void close() throws Exception {
        super.close();
    }
}
