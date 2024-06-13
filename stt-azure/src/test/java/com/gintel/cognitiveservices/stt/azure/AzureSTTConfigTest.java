package com.gintel.cognitiveservices.stt.azure;
import static org.junit.Assert.assertNotNull;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Test;

public class AzureSTTConfigTest {
    @Test
    public void readTest() {
        ConfigFactory.setProperty("config_file", "../conf/web.properties");
        AzureSTTConfig config = ConfigFactory.create(AzureSTTConfig.class);
        assertNotNull(config.region());
    }


    
}

