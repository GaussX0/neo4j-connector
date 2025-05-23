package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.connect.sdk.api.Record;
import com.ibm.connect.sdk.api.RowBasedSourceInteraction;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;
import org.apache.arrow.flight.Ticket;
import org.neo4j.driver.Session;

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
        return List.of(new Ticket(getAsset().getConnectionProperties().toString().getBytes())); // TODO + path
    }

    @Override
    public List<CustomFlightAssetField> getFields() {
        List<CustomFlightAssetField> fields = new ArrayList<>();

        // TODO: Implement logic to retrieve fields
        // 1. wektory, krawedzie
        // 2. typ

        return fields;
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
