package org.noear.folkmq.middleware.broker.common;

import org.noear.socketd.utils.StrUtils;
import org.noear.solon.Solon;

import java.util.Map;

/**
 * @author noear
 * @since 1.0
 */
public class MqBrokerConfig {
    public static final String accessAk;
    public static final String accessSk;

    public static final String apiToken;

    public static final int ioThreads;
    public static final int codecThreads;
    public static final int exchangeThreads;

    static {
        accessAk = Solon.cfg().get(ConfigNames.folkmq_access_ak);
        accessSk = Solon.cfg().get(ConfigNames.folkmq_access_sk);

        ioThreads = Solon.cfg().getInt(ConfigNames.folkmq_ioThreads, 1);
        codecThreads = Solon.cfg().getInt(ConfigNames.folkmq_codecThreads, 1);
        exchangeThreads = Solon.cfg().getInt(ConfigNames.folkmq_exchangeThreads, 1);

        apiToken = Solon.cfg().get(ConfigNames.folkmq_api_token, "");
    }

    public static Map<String, String> getAccessMap() {
        Map<String, String> accessMap = Solon.cfg().getMap(ConfigNames.folkmq_access_x);
        accessMap.remove("ak");
        accessMap.remove("sk");

        if (StrUtils.isNotEmpty(MqBrokerConfig.accessAk)) {
            accessMap.put(MqBrokerConfig.accessAk, MqBrokerConfig.accessSk);
        }

        return accessMap;
    }
}
