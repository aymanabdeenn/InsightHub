package com.ayman.datasourceservice.domain;

public class PoolStats {
    private int activeConnections;
    private int idleConnections;
    private int totalConnections;
    private int waitingThreads;

    public PoolStats() {
    }

    public PoolStats(int activeConnections, int idleConnections, int totalConnections, int waitingThreads) {
        this.activeConnections = activeConnections;
        this.idleConnections = idleConnections;
        this.totalConnections = totalConnections;
        this.waitingThreads = waitingThreads;
    }

    public int getActiveConnections() { return activeConnections; }
    public int getIdleConnections() { return idleConnections; }
    public int getTotalConnections() { return totalConnections; }
    public int getWaitingThreads() { return waitingThreads; }
}
