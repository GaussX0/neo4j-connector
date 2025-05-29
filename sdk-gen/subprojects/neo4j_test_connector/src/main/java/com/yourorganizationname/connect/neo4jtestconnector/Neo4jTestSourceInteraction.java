package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.connect.sdk.api.Record;
import com.ibm.connect.sdk.api.RowBasedSourceInteraction;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;
import org.apache.arrow.flight.Ticket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Neo4jTestSourceInteraction extends RowBasedSourceInteraction<Neo4jTestConnector> {
    private boolean hasNext = true;
    private final Neo4jResourceMapper resourceMapper = new Neo4jResourceMapper();
    public Neo4jTestSourceInteraction(Neo4jTestConnector connector, CustomFlightAssetDescriptor asset) {
        super();
        setConnector(connector);
        setAsset(asset);
    }

    @Override
    public List<Ticket> getTickets() {
        Ticket ticket = new Ticket(String.format("{\"request_id\": \"%s\"}", UUID.randomUUID()).getBytes());
        System.out.println(ticket);
        System.out.println(Arrays.toString(ticket.getBytes()));
        return List.of(ticket);
    }

    @Override
    public List<CustomFlightAssetField> getFields() {

        // TODO: Implement logic to retrieve fields
        // 1. wektory, krawedzie
        // 2. typ
        Neo4jPath path = new Neo4jPath(getAsset().getPath());
        ConnectionProperties connectionProperties = getAsset().getConnectionProperties();
        List<CustomFlightAssetField> fields = resourceMapper.getFields(connectionProperties, path);
        return fields;
    }

    @Override
    public Record getRecord() {
        System.out.println("getRecord");
        if (hasNext) {
            hasNext = false;
            // TODO: implement records as JSONs from neo4j
//            Record record = new Record();
//            record.appendValue(123);
            return new Record();
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        super.close();
    }
}
