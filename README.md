A connector meant to be a communication interface between a Neo4J database and an IBM AI model.

### Running the Project:
1. Navigate to the `sdk-gen` folder and run the Gradle build command:

`./gradlew :wdp-connect-sdk-gen-neo4j_test_connectorflight:dockerBuild`

2. Next run: 

`./gradlew :wdp-connect-sdk-gen-neo4j_test_connectorflight:dockerStart`

The connector container should now be created and ready to use.

3. Create and run a Neo4j container:

```bash
docker run --name neo4j --publish=7474:7474 --publish=7687:7687 --env NEO4J_AUTH=neo4j/neo4j123 neo4j
```
4. Open your browser and go to: `http://localhost:7474/browser/`. Log in with credentials provided previously (login: neo4j, password: neo4j123)
5. Click the star icon on the left panel, and under the `Example Graphs` section, select `Movie Graph`.
   This will create an example database.
6. Open `client.ipynb` file and run all the cells to test the connector.
