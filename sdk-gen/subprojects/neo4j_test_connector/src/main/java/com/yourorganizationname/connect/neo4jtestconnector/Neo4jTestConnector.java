/* *************************************************** */

/* (C) Copyright IBM Corp. 2022                        */

/* *************************************************** */
package com.yourorganizationname.connect.neo4jtestconnector;

import java.util.List;

import org.apache.arrow.flight.Ticket;

import com.ibm.connect.sdk.api.RowBasedConnector;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionActionConfiguration;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionActionResponse;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetsCriteria;

@SuppressWarnings({ "PMD.AvoidDollarSigns", "PMD.ClassNamingConventions" })
public class Neo4jTestConnector
        extends RowBasedConnector<Neo4jTestSourceInteraction, Neo4jTestTargetInteraction>
{
    /**
     * Creates a row-based connector.
     *
     * @param properties
     *            connection properties
     */
    public Neo4jTestConnector(ConnectionProperties properties)
    {
        super(properties);
    }

    private final Neo4jResourceMapper resourceMapper = new Neo4jResourceMapper();

    @Override
    public void close() throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void connect() throws Exception {
        resourceMapper.connect(this.getConnectionProperties());
    }

    @Override
    public List<CustomFlightAssetDescriptor> discoverAssets(CustomFlightAssetsCriteria criteria) throws Exception
    {
        return resourceMapper.getAssetDescriptors(criteria.getConnectionProperties(), criteria.getPath());
    }

    // TODO: Method to implement for flight_info and do_get
    // the connect method will be run first
    @Override
    public Neo4jTestSourceInteraction getSourceInteraction(CustomFlightAssetDescriptor asset, Ticket ticket)
    {
        // TODO include your ticket info
        // if ticket is null then return tickets
        return new Neo4jTestSourceInteraction(this, asset, this.resourceMapper);
    }

    @Override
    public Neo4jTestTargetInteraction getTargetInteraction(CustomFlightAssetDescriptor asset)
    {
        return new Neo4jTestTargetInteraction(this, asset);
    }

    @Override
    public ConnectionActionResponse performAction(String action, ConnectionActionConfiguration conf)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void commit()
    {
        // TODO Auto-generated method stub

    }

}
