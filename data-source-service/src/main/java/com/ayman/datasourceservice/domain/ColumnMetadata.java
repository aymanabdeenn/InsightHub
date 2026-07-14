package com.ayman.datasourceservice.domain;

public class ColumnMetadata {
    String columnName;
    String dataType;
    boolean nullable;

    public ColumnMetadata() {}

    public ColumnMetadata(String columnName, String dataType, boolean nullable) {
        this.columnName = columnName;
        this.dataType = dataType;
        this.nullable = nullable;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }
}
