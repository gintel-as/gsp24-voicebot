package com.gintel.cognitiveservices.tts.aws;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReloadType;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import com.gintel.cognitiveservices.config.ConfigTrim;

@Config.HotReload(unit = TimeUnit.MINUTES, value = 1, type = HotReloadType.SYNC)
@Config.PreprocessorClasses({ ConfigTrim.class })
@Sources("file:${config_file}")
public interface AWSTTSConfig extends Reloadable {
    @Key("aws.stt.profile_user_name")
    String userName();

    @Key("aws.stt.region")
    String region();

    @Key("aws.stt.access_key_id")
    String accessKey();

    @Key("aws.stt.secret_access_key_id")
    String secretKey();
}