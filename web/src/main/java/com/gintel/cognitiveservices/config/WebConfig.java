package com.gintel.cognitiveservices.config;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReloadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

@Config.HotReload(unit = TimeUnit.MINUTES, value = 1, type = HotReloadType.SYNC)
@Config.PreprocessorClasses({ConfigTrim.class})
@Sources("file:${config_file}")
public interface WebConfig extends Config, Reloadable {
    @Key("some.config.property")
    @DefaultValue("some.default.value")
    String someConfigProperty();
}
