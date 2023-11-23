package org.noear.folkmq.server;

import org.noear.folkmq.MqConstants;
import org.noear.socketd.SocketD;
import org.noear.socketd.transport.core.Message;
import org.noear.socketd.transport.core.Session;
import org.noear.socketd.transport.core.entity.StringEntity;
import org.noear.socketd.transport.core.listener.BuilderListener;
import org.noear.socketd.transport.server.Server;
import org.noear.socketd.utils.RunUtils;

import java.io.IOException;
import java.util.*;

/**
 * @author noear
 * @since 1.0
 */
public class MqServerImpl extends BuilderListener implements MqServer {
    private Server server;
    private Map<String, Set<String>> subscribeMap = new HashMap<>();
    private Map<String, MqMessageQueue> identityMap = new HashMap<>();

    private Map<String, String> accessMap = new HashMap<>();


    @Override
    public MqServer addAccess(String accessKey, String accessSecretKey) {
        accessMap.put(accessKey, accessSecretKey);
        return this;
    }

    @Override
    public MqServer stop() {
        server.stop();
        return this;
    }

    @Override
    public MqServer start(int port) throws Exception {
        server = SocketD.createServer("sd:tcp")
                .config(c -> c.port(port))
                .listen(this)
                .start();

        //接收订阅指令
        on(MqConstants.MQ_CMD_SUBSCRIBE, (s, m) -> {
            if (m.isRequest() || m.isSubscribe()) {
                //表示我收到了
                s.replyEnd(m, new StringEntity(""));
            }

            String topic = m.meta(MqConstants.MQ_TOPIC);
            String identity = m.meta(MqConstants.MQ_IDENTITY);

            onSubscribe(topic, identity, s);
        });

        //接收发布指令
        on(MqConstants.MQ_CMD_PUBLISH, (s, m) -> {
            if (m.isRequest() || m.isSubscribe()) {
                //表示我收到了
                RunUtils.asyncAndTry(()->{
                    s.replyEnd(m, new StringEntity(""));
                });
            }

            String topic = m.meta(MqConstants.MQ_TOPIC);
            onPublish(topic, m);
        });

        return this;
    }

    @Override
    public void onOpen(Session session) throws IOException {
        super.onOpen(session);

        if (accessMap.size() > 0) {
            //如果有 ak/sk 配置，则进行鉴权
            String accessKey = session.param(MqConstants.PARAM_ACCESS_KEY);
            String accessSecretKey = session.param(MqConstants.PARAM_ACCESS_SECRET_KEY);

            if (accessKey == null || accessSecretKey == null) {
                session.close();
                return;
            }

            if (accessSecretKey.equals(accessMap.get(accessKey)) == false) {
                session.close();
                return;
            }
        }
    }

    @Override
    public void onClose(Session session) {
        super.onClose(session);

        //遍历这个会话身上的身份记录（有些可能不是）
        for (String identity : session.attrMap().keySet()) {
            MqMessageQueue messageQueue = identityMap.get(identity);

            //如果找到对应的队列
            if (messageQueue != null) {
                messageQueue.removeSubscriber(session);
            }
        }
    }

    /**
     * 当订阅时
     */
    private synchronized void onSubscribe(String topic, String identity, Session session) {
        //给会话添加身份（可以有多个不同的身份）
        session.attr(identity, "1");

        //以身份进行订阅(topic -> identity)
        Set<String> identitySet = subscribeMap.get(topic);
        if (identitySet == null) {
            identitySet = new HashSet<>();
            subscribeMap.put(topic, identitySet);
        }

        identitySet.add(identity);

        //为身份建立队列(identity -> queue)
        if (identityMap.containsKey(identity) == false) {
            MqMessageQueue messageQueue = new MqMessageQueueImpl(identity);
            messageQueue.addSubscriber(session);
            identityMap.put(identity, messageQueue);
        }
    }

    /**
     * 当发布时
     */
    private void onPublish(String topic, Message message) throws IOException {
        //取出所有订阅的身份
        Set<String> identitySet = subscribeMap.get(topic);
        if (identitySet != null) {
            for (String identity : identitySet) {
                MqMessageQueue queue = identityMap.get(identity);
                if (queue != null) {
                    MqMessageHolder messageHolder = new MqMessageHolder(message);
                    queue.push(messageHolder);
                }
            }
        }
    }
}