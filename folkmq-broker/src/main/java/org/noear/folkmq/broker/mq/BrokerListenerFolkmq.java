package org.noear.folkmq.broker.mq;

import org.noear.folkmq.common.MqConstants;
import org.noear.socketd.broker.BrokerListener;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * FolkMq 经纪人监听
 *
 * @author noear
 * @since 1.0
 */
public class BrokerListenerFolkmq extends BrokerListener {
    //服务端访问账号
    private Map<String, String> serverAccessMap = new HashMap<>();

    /**
     * 配置访问账号
     *
     * @param accessKey       访问者身份
     * @param accessSecretKey 访问者密钥
     */
    public BrokerListenerFolkmq addAccess(String accessKey, String accessSecretKey) {
        serverAccessMap.put(accessKey, accessSecretKey);
        return this;
    }

    /**
     * 配置访问账号
     *
     * @param accessMap 访问账号集合
     */
    public BrokerListenerFolkmq addAccessAll(Map<String, String> accessMap) {
        if (accessMap != null) {
            serverAccessMap.putAll(accessMap);
        }
        return this;
    }

    @Override
    public void onOpen(Session session) throws IOException {
        super.onOpen(session);

        String atName = session.at();
        if (MqConstants.BROKER_AT_SERVER.equals(atName)) {
            return;
        }

        if (serverAccessMap.size() > 0) {
            //如果有 ak/sk 配置，则进行鉴权
            String accessKey = session.param(MqConstants.PARAM_ACCESS_KEY);
            String accessSecretKey = session.param(MqConstants.PARAM_ACCESS_SECRET_KEY);

            if (accessKey == null || accessSecretKey == null) {
                session.close();
                return;
            }

            if (accessSecretKey.equals(serverAccessMap.get(accessKey)) == false) {
                session.close();
                return;
            }
        }

        log.info("Server channel opened, sessionId={}", session.sessionId());
    }

    @Override
    public void onClose(Session session) {
        super.onClose(session);

        Collection<String> atList = session.attrMap().keySet();
        if (atList.size() > 0) {
            for (String at : atList) {
                //注销服务
                removeService(at, session);
            }
        }
    }

    @Override
    public void onMessage(Session requester, Message message) throws IOException {
        if (MqConstants.MQ_EVENT_SUBSCRIBE.equals(message.event())) {
            //订阅，注册服务
            String consumer = message.meta(MqConstants.MQ_META_CONSUMER);
            requester.attr(consumer, "1");
            addService(consumer, requester);
        } else if (MqConstants.MQ_EVENT_UNSUBSCRIBE.equals(message.event())) {
            //取消订阅，注销服务
            String consumer = message.meta(MqConstants.MQ_META_CONSUMER);
            removeService(consumer, requester);
        }

        super.onMessage(requester, message);
    }
}
