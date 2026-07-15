package com.ayman.datasourceservice.security;

import com.ayman.datasourceservice.domain.ConnectionConfig;
import com.ayman.datasourceservice.domain.PlainConnectionConfig;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CredentialEncryptionService {
    private final StringEncryptor stringEncryptor;

    @Autowired
    public CredentialEncryptionService(StringEncryptor stringEncryptor) {
        this.stringEncryptor = stringEncryptor;
    }

    public ConnectionConfig encrypt(PlainConnectionConfig plain) {
        return new ConnectionConfig(
                plain.getHost(),
                plain.getPort(),
                plain.getDatabase(),
                plain.getUsername(),
                stringEncryptor.encrypt(plain.getPassword())
        );
    }

    public PlainConnectionConfig decrypt(ConnectionConfig securedConfig) {
        return new PlainConnectionConfig(
                securedConfig.getHost(),
                securedConfig.getPort(),
                securedConfig.getDatabase(),
                securedConfig.getUsername(),
                stringEncryptor.decrypt(securedConfig.getPasswordEncrypted())
        );
    }
}
