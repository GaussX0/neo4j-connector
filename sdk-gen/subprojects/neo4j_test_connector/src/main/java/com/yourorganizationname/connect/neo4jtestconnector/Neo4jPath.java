package com.yourorganizationname.connect.neo4jtestconnector;

public class Neo4jPath {
    private final String path;
    private String type;
    private String name;

    public Neo4jPath(String path) {
        this.path = path;
        if (path == null) {
            return;
        }
        String[] parts = path.split("/");
        if (parts.length >= 2) {
            type = parts[1];
        }
        if (parts.length >= 3) {
            name = parts[2];
        }
    }

    public boolean isGetAll(){
        return type == null && name == null;
    }

    public boolean isGetType(){
        return type != null && name == null;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
