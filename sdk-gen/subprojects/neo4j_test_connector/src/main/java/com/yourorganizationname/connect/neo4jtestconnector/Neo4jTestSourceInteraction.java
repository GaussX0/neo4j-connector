package com.yourorganizationname.connect.neo4jtestconnector;

import com.ibm.connect.sdk.api.Record;
import com.ibm.connect.sdk.api.RowBasedSourceInteraction;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetField;
import org.apache.arrow.flight.Ticket;

import java.io.Serializable;
import java.util.*;

public class Neo4jTestSourceInteraction extends RowBasedSourceInteraction<Neo4jTestConnector> {
    private final Neo4jResourceMapper mapper;
    private final Neo4jPath path;

    private List<Map<String, Object>> records = new ArrayList<>();
    private int iterator = 0;
    private List<CustomFlightAssetField> fields = new ArrayList<>();

    public Neo4jTestSourceInteraction(Neo4jTestConnector connector, CustomFlightAssetDescriptor asset, Neo4jResourceMapper mapper) {
        super();
        setConnector(connector);
        setAsset(asset);
        this.mapper = mapper;
        this.path = new Neo4jPath(this.getAsset().getPath());
        this.records = this.mapper.getValuesForPath(this.getAsset().getConnectionProperties(), this.path);
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
        this.fields = mapper.getFields(this.getAsset().getConnectionProperties(), this.path);
        return fields;
    }

    @Override
    public Record getRecord() {
        if (this.iterator < this.records.size()) {
            Record record = new Record();
            Map<String, Object> recordMap = this.records.get(this.iterator);
            for (CustomFlightAssetField field: fields) {
                record.appendValue((Serializable) recordMap.get(field.getName()));
            }
            iterator++;
            return record;
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
