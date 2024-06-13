package com.gintel.cognitiveservices.tts.azure;

import static org.junit.Assert.assertNotNull;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

public class AzureTTSConfigTest {
    @Test
    public void readTest() {
        ConfigFactory.setProperty("config_file", "../config/web.properties");
        AzureTTSConfig config = ConfigFactory.create(AzureTTSConfig.class);
        assertNotNull(config.region());
    }
}
