package com.ayman.datasourceservice.connector;

import com.ayman.configlib.error.ApiException;
import com.ayman.datasourceservice.domain.ConnectorType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConnectorFactory {
    private final Map<ConnectorType, DataConnector> connectors;

    public ConnectorFactory(List<DataConnector> list) {
        this.connectors = list.stream()
                .collect(Collectors.toMap(DataConnector::getType, c -> c));
    }

    public DataConnector get(ConnectorType type) {
        DataConnector connector = connectors.get(type);
        if(connector == null) {
            throw new ApiException("UNSUPPORTED_CONNECTOR_TYPE", "No connector is registered for type: " + type);
        }
        return connectors.get(type);
    }
}
