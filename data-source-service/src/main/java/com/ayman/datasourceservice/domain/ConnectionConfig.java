package com.ayman.datasourceservice.domain;

public class ConnectionConfig {
    private String host;
    private int port;
    private String database;
    private String username;
    private String passwordEncrypted;

    public ConnectionConfig() {}

    public ConnectionConfig(String host, int port, String database, String username, String passwordEncrypted) {
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.passwordEncrypted = passwordEncrypted;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordEncrypted() {
        return passwordEncrypted;
    }

    public void setPasswordEncrypted(String passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }
}
