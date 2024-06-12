package com.gintel.cognitiveservices.tts.azure;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReloadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import com.gintel.cognitiveservices.config.ConfigTrim;

@Config.HotReload(unit = TimeUnit.MINUTES, value = 1, type = HotReloadType.SYNC)
@Config.PreprocessorClasses({ConfigTrim.class})
@Sources("file:${config_file}")
public interface AzureTTSConfig extends Config, Reloadable {
    @Key("azure.tts.subscription_key")
    @DefaultValue("b16f6e70cac14487af395758c3db4e59")
    String subscriptionKey();

    @Key("azure.tts.region")
    @DefaultValue("norwayeast")
    String region();
}