package org.noear.folkmq.proxy.middleware.common;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.utils.StrUtils;
import org.noear.solon.Solon;

import java.util.Map;

/**
 * @author noear
 * @since 1.0
 */
public class MqProxyConfig {
    public static final String accessAk;
    public static final String accessSk;

    public static final String apiToken;

    public static final int ioThreads;
    public static final int codecThreads;
    public static final int exchangeThreads;

    public static final long streamTimeout;

    static {
        accessAk = Solon.cfg().get(MqConfigNames.folkmq_access_ak);
        accessSk = Solon.cfg().get(MqConfigNames.folkmq_access_sk);

        apiToken = Solon.cfg().get(MqConfigNames.folkmq_api_token, "");

        ioThreads = Solon.cfg().getInt(MqConfigNames.folkmq_ioThreads, 1);
        codecThreads = Solon.cfg().getInt(MqConfigNames.folkmq_codecThreads, 1);
        exchangeThreads = Solon.cfg().getInt(MqConfigNames.folkmq_exchangeThreads, 1);

        streamTimeout = Solon.cfg().getLong(MqConfigNames.folkmq_streamTimeout, MqConstants.SERVER_STREAM_TIMEOUT_DEFAULT);
    }

    public static Map<String, String> getAccessMap() {
        Map<String, String> accessMap = Solon.cfg().getMap(MqConfigNames.folkmq_access_x);
        accessMap.remove("ak");
        accessMap.remove("sk");

        if (StrUtils.isNotEmpty(MqProxyConfig.accessAk)) {
            accessMap.put(MqProxyConfig.accessAk, MqProxyConfig.accessSk);
        }

        return accessMap;
    }
}
