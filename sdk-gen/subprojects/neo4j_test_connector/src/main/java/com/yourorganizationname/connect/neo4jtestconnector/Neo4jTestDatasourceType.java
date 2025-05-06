/* *************************************************** */

/* (C) Copyright IBM Corp. 2022                        */

/* *************************************************** */
package com.yourorganizationname.connect.neo4jtestconnector;

import java.util.Collections;


import com.ibm.wdp.connect.common.sdk.api.models.*;
import com.ibm.wdp.connect.common.sdk.api.models.CustomDatasourceTypeProperty.TypeEnum;

@SuppressWarnings({ "PMD.AvoidDollarSigns", "PMD.ClassNamingConventions" })
public class Neo4jTestDatasourceType extends CustomFlightDatasourceType
{
    /**
     * An instance of the custom Apache Derby data source type.
     */
    public static final Neo4jTestDatasourceType INSTANCE = new Neo4jTestDatasourceType();

    /**
     * The unique identifier name of the data source type.
     */
    public static final String DATASOURCE_TYPE_NAME = "neo4j_test_connector";
    private static final String DATASOURCE_TYPE_LABEL = "Neo4J Test Connector";
    private static final String DATASOURCE_TYPE_DESCRIPTION = "Neo4j Test Connector meant to be to be a communication interface between a Neo4J database and an IBM AI model";

    public Neo4jTestDatasourceType()
    {
        super();

        // Set the data source type attributes.
        setName(DATASOURCE_TYPE_NAME);
        setLabel(DATASOURCE_TYPE_LABEL);
        setDescription(DATASOURCE_TYPE_DESCRIPTION);
        setAllowedAsSource(true);
        setAllowedAsTarget(false);
        setStatus(CustomFlightDatasourceType.StatusEnum.PENDING);
        setTags(Collections.emptyList());
        setDiscovery(new DatasourceTypeDiscovery()
                .topLevelFilters(Collections.emptyList())
                .assetTypes(Collections.emptyList())
                .pathProperties(Collections.emptyList()));
        final CustomFlightDatasourceTypeProperties properties = new CustomFlightDatasourceTypeProperties();
        setProperties(properties);
        // Define the connection properties.
        // TODO adjust these properties for your scenario
        properties.addConnectionItem(new CustomDatasourceTypeProperty()
                .name("host")
                .label("Host")
                .description("Neo4j server hostname or IP")
                .type(TypeEnum.STRING)
                .required(true)
                .masked(false)
                .defaultValue("localhost")
                .placeholder("")
                .multiline(false)
                .values(Collections.emptyList())
                .group("connection"));

        properties.addConnectionItem(new CustomDatasourceTypeProperty()
                .name("port")
                .label("Port")
                .description("Neo4j Bolt port (usually 7687)")
                .type(TypeEnum.INTEGER)
                .required(true)
                .masked(false)
                .defaultValue("7687")
                .placeholder("")
                .multiline(false)
                .values(Collections.emptyList())
                .group("connection"));

        properties.addConnectionItem(new CustomDatasourceTypeProperty()
                .name("username")
                .label("Username")
                .description("Username for Neo4j authentication")
                .type(TypeEnum.STRING)
                .required(true)
                .masked(false)
                .defaultValue("")
                .placeholder("")
                .multiline(false)
                .values(Collections.emptyList())
                .group("credentials"));

        properties.addConnectionItem(new CustomDatasourceTypeProperty()
                .name("password")
                .label("Password")
                .description("Password for Neo4j authentication")
                .type(TypeEnum.STRING)
                .required(true)
                .masked(true)
                .defaultValue("")
                .placeholder("")
                .multiline(false)
                .values(Collections.emptyList())
                .group("credentials"));

        properties.addConnectionItem(new CustomDatasourceTypeProperty()
                .name("ssl")
                .label("Port is SSL-enabled")
                .description("The port is configured to accept SSL connections")
                .type(TypeEnum.BOOLEAN)
                .required(false)
                .masked(false)
                .defaultValue("")
                .placeholder("")
                .multiline(false)
                .values(Collections.emptyList())
                .group("ssl"));

        properties.addConnectionItem(new CustomDatasourceTypeProperty()
                .name("ssl_certificate")
                .label("SSL certificate")
                .description("The SSL certificate of the host to be trusted which is only needed when the host certificate was not signed by a known certificate authority")
                .type(TypeEnum.STRING)
                .required(false)
                .masked(false)
                .defaultValue("")
                .placeholder("")
                .multiline(true)
                .values(Collections.emptyList())
                .group("ssl"));

        // TODO: add some actions in the future
        setActions(Collections.emptyList());

        properties.setSource(Collections.emptyList());
        properties.setTarget(Collections.emptyList());
        properties.setFilter(Collections.emptyList());
    }
}
