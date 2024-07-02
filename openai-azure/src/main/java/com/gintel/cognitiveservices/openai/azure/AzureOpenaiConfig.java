package com.gintel.cognitiveservices.openai.azure;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReloadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import com.gintel.cognitiveservices.config.ConfigTrim;

@Config.HotReload(unit = TimeUnit.MINUTES, value = 1, type = HotReloadType.SYNC)
@Config.PreprocessorClasses({ConfigTrim.class})
@Sources("file:${config_file}")

public interface AzureOpenaiConfig extends Reloadable{
    
    @Key("azure.openai.subscription_key")
    String subscriptionKey();

    @Key("azure.openai.region")
    String region();
}
