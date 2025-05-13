/* *************************************************** */

/* (C) Copyright IBM Corp. 2022                        */

/* *************************************************** */
package com.yourorganizationname.connect.neo4jtestconnector;

import java.util.ArrayList;
import java.util.List;

import org.apache.arrow.flight.Ticket;

import com.ibm.connect.sdk.api.RowBasedConnector;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionActionConfiguration;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionActionResponse;
import com.ibm.wdp.connect.common.sdk.api.models.ConnectionProperties;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetDescriptor;
import com.ibm.wdp.connect.common.sdk.api.models.CustomFlightAssetsCriteria;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

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

    private Neo4jResourceMapper resourceMapper = new Neo4jResourceMapper();

    @Override
    public void close() throws Exception
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void connect() throws Exception {
        // TODO Auto-generated method stub
        resourceMapper.testConnection(this.getConnectionProperties());
    }

    @Override
    public List<CustomFlightAssetDescriptor> discoverAssets(CustomFlightAssetsCriteria criteria) throws Exception
    {
        return resourceMapper.getAssetDescriptors(criteria.getConnectionProperties(), criteria.getPath());
    }

    @Override
    public Neo4jTestSourceInteraction getSourceInteraction(CustomFlightAssetDescriptor asset, Ticket ticket)
    {
        // TODO include your ticket info
        return new Neo4jTestSourceInteraction(this, asset);
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
