package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.connect.sdk.api.Record;
import com.ibm.connect.sdk.api.RowBasedSourceInteraction;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;
import org.apache.arrow.flight.Ticket;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Neo4jTestSourceInteraction extends RowBasedSourceInteraction<Neo4jTestConnector> {

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

        return new ArrayList<>();
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
