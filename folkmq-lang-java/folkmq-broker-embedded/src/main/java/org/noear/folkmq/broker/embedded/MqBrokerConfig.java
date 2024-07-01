package org.noear.folkmq.broker.embedded;

import org.noear.folkmq.broker.embedded.mq.FolkmqLifecycleBean;
import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.utils.StrUtils;
import org.noear.solon.Solon;
import org.noear.solon.core.util.PathUtil;

import java.util.Collections;
import java.util.Map;

/**
 * @author noear
 * @since 1.0
 */
public class MqBrokerConfig {
    public static boolean isStandalone() {
        return FolkmqLifecycleBean.isStandalone();
    }

    public static final String path;
    public static final String displayPath;

    public static final String accessAk;
    public static final String accessSk;

    public static final String apiToken;

    public static final int ioThreads;
    public static final int codecThreads;
    public static final int exchangeThreads;

    public static final long streamTimeout;

    public static final boolean saveEnable;
    public static final long save900;
    public static final long save300;
    public static final long save100;


    public static final String proxyServer;
    public static final int folkmqTransportPort;

    static {
        path = Solon.cfg().get(MqConfigNames.folkmq_path);
        String displayPathStr = PathUtil.mergePath(Solon.cfg().serverContextPath(), path);
        if (displayPathStr.endsWith("/")) {
            displayPath = displayPathStr.substring(0, displayPathStr.length() - 1);
        } else {
            displayPath = displayPathStr;
        }

        String proxyServerTmp = Solon.cfg().get(MqConfigNames.folkmq_proxy);
        if (StrUtils.isEmpty(proxyServerTmp)) {
            proxyServer = Solon.cfg().get("folkmq.broker");//向下兼容
        } else {
            proxyServer = proxyServerTmp;
        }
        folkmqTransportPort = Solon.cfg().getInt(MqConfigNames.folkmq_transport_port, 0);

        saveEnable = Solon.cfg().getBool(MqConfigNames.folkmq_snapshot_enable, true);
        save900 = Solon.cfg().getLong(MqConfigNames.folkmq_snapshot_save900, 0);
        save300 = Solon.cfg().getLong(MqConfigNames.folkmq_snapshot_save300, 0);
        save100 = Solon.cfg().getLong(MqConfigNames.folkmq_snapshot_save100, 0);

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

        String ak = Solon.cfg().get(MqConfigNames.folkmq_access_ak);
        String sk = Solon.cfg().get(MqConfigNames.folkmq_access_sk);

        if (StrUtils.isNotEmpty(ak)) {
            accessMap.put(ak, sk);
        }

        return Collections.unmodifiableMap(accessMap);
    }
}