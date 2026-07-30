package com.ayman.configlib.error;

import java.sql.SQLException;

public class DataSourceConnectionException extends RuntimeException{


    private String errorCode;
    private String developerMessage;
    private String failureReason;

    public DataSourceConnectionException(String errorCode, String developerMessage, String failureReason) {
        this.errorCode = errorCode;
        this.developerMessage = developerMessage;
        this.failureReason = failureReason;
    }

    public String getErrorCode() { return errorCode; }
    public String getDeveloperMessage() { return developerMessage; }
    public String getFailureReason() { return failureReason; }

    /**
     * Inspects the raw SQLException returned by the target database driver
     * and maps it to a human-readable failureReason.
     */
    public static DataSourceConnectionException fromDatabaseError(Throwable throwable) {
        // 1. Unwrap the exception chain to find the underlying SQLException
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && !(rootCause instanceof SQLException)) {
            rootCause = rootCause.getCause();
        }

        if (rootCause instanceof SQLException sqlEx) {
            String sqlState = sqlEx.getSQLState();
            int vendorErrorCode = sqlEx.getErrorCode();
            String dbMessage = sqlEx.getMessage();

            String failureReason = parseReasonFromSqlException(sqlState, vendorErrorCode, dbMessage);

            return new DataSourceConnectionException(
                    "CONNECTION_TEST_FAILED",
                    "Database error: " + dbMessage,
                    failureReason
            );
        }

        // Fallback for non-SQL network failures or timeouts
        return new DataSourceConnectionException(
                "CONNECTION_TEST_FAILED",
                throwable.getMessage(),
                "UNKNOWN_HOST_OR_NETWORK_ERROR"
        );
    }

    private static String parseReasonFromSqlException(String sqlState, int errorCode, String dbMessage) {
        if (sqlState != null) {
            // ANSI SQL Standard Class 28 -> Invalid Authorization / Bad Password / Unknown User
            if (sqlState.startsWith("28")) {
                return "INVALID_CREDENTIALS";
            }
            // ANSI SQL Standard Class 08 -> Connection Exception / Host unreachable / Refused
            if (sqlState.startsWith("08")) {
                return "HOST_UNREACHABLE_OR_PORT_CLOSED";
            }
            // ANSI SQL Standard Class 3D -> Invalid Catalog Name (Database does not exist)
            if (sqlState.startsWith("3D")) {
                return "DATABASE_NOT_FOUND";
            }
        }

        // Additional checks based on common vendor messages if SQLState is missing
        if (dbMessage != null) {
            String lowerMsg = dbMessage.toLowerCase();
            if (lowerMsg.contains("password") || lowerMsg.contains("denied") || lowerMsg.contains("authentication")) {
                return "INVALID_CREDENTIALS";
            }
            if (lowerMsg.contains("does not exist") || lowerMsg.contains("unknown database")) {
                return "DATABASE_NOT_FOUND";
            }
            if (lowerMsg.contains("refused") || lowerMsg.contains("timed out") || lowerMsg.contains("unreachable")) {
                return "HOST_UNREACHABLE_OR_PORT_CLOSED";
            }
        }

        return "DATABASE_REJECTED_CONNECTION";
    }
}
