package com.gintel.cognitiveservices.stt.aws;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReloadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import com.gintel.cognitiveservices.config.ConfigTrim;

@Config.HotReload(unit = TimeUnit.MINUTES, value = 1, type = HotReloadType.SYNC)
@Config.PreprocessorClasses({ ConfigTrim.class })
@Sources("file:${config_file}")

public interface AWSSTTConfig extends Reloadable {

    @Key("aws.stt.access_key_id")
    String accessKeyId();

    @Key("aws.stt.secret_access_key")
    public String secretAccessKey();

    @Key("aws.stt.region")
    String region();
}
