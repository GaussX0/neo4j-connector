package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.connect.sdk.api.Record;
import com.ibm.connect.sdk.api.RowBasedSourceInteraction;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;
import org.apache.arrow.flight.Ticket;
import org.neo4j.driver.Result;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Neo4jTestSourceInteraction extends RowBasedSourceInteraction<Neo4jTestConnector> {
    private final Neo4jResourceMapper mapper;
    private final Neo4jPath path;
    private List<org.neo4j.driver.Record> queryResult = null;
    private int index = 0;
    private boolean queryRan = false;

    public Neo4jTestSourceInteraction(Neo4jTestConnector connector, CustomFlightAssetDescriptor asset, Neo4jResourceMapper mapper) {
        super();
        setConnector(connector);
        setAsset(asset);
        this.mapper = mapper;
        this.path = new Neo4jPath(this.getAsset().getPath());
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
        return mapper.getFields(this.getAsset().getConnectionProperties(), this.path);
    }

    @Override
    public Record getRecord() {
        if (queryRan && queryResult.size() < index -1 ) {
            return null;
        }
        if (!queryRan){
            queryRan = true;
            queryResult = this.mapper.getValuesForPath(this.getAsset().getConnectionProperties(), this.path);
            index = 0;
        }
        if (queryResult.size() < index -1 ) {
            return this.createRecordFromNeo4j(queryResult.get(index++));
        }
        return null;
    }

    private Record createRecordFromNeo4j(org.neo4j.driver.Record record){
        Record ret = new Record();
        record.values().forEach(
                value -> ret.appendValue(value.asString())
        );
        return ret;
    }

    @Override
    public void close() throws Exception {
        super.close();
    }
}
