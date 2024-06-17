package com.gintel.cognitiveservices.openai.azure;

import static org.junit.Assert.assertNotNull;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

public class AzureOpenaiConfigTest {
    @Test
    public void readTest() {
        ConfigFactory.setProperty("config_file", "../conf/web.properties");
        AzureOpenaiConfig config = ConfigFactory.create(AzureOpenaiConfig.class);
        assertNotNull(config.region());
    }
}
