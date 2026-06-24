package com.ayman.configlib.response;

import com.ayman.configlib.i18n.YamlPropertySourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "responses")
@PropertySource(value = "classpath:responses.yml", factory = YamlPropertySourceFactory.class)
public class ResponseCodeProperties {
    private Map<String, ResponseDetail> codes = new LinkedHashMap<>();

    public Map<String, ResponseDetail> getCodes() {
        return codes;
    }

    public void setCodes(Map<String, ResponseDetail> codes) {
        this.codes = codes;
    }

    public static class ResponseDetail {
        private int status;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
