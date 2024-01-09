import {EventListener} from "@noear/socket.d/transport/core/Listener";
import {MqClientDefault} from "./MqClient";
import {MqConstants} from "../common/MqConstants";
import {MqMessageReceivedImpl} from "./IMqMessage";
import {Session} from "@noear/socket.d/transport/core/Session";
import {SocketD} from "@noear/socket.d";
import {SocketdAlarmException} from "@noear/socket.d/exception/SocketdException";

export class MqClientListener extends EventListener {
    private _client: MqClientDefault;

    constructor(client: MqClientDefault) {
        super();
        this._client = client;

        //接收派发指令
        this.doOn(MqConstants.MQ_EVENT_DISTRIBUTE, (s, m) => {
            let message: MqMessageReceivedImpl | null = null;

            try {
                message = new MqMessageReceivedImpl(client, s, m);
                let subscription = client._subscriptionMap.get(message.getTopic());

                if (subscription != null) {
                    subscription.consume(message);
                }

                //是否自动回执
                if (client._autoAcknowledge) {
                    client.acknowledge(s, m, message, true);
                }
            } catch (e) {
                if (message != null) {
                    client.acknowledge(s, m, message, false);
                    console.warn("Client consume handle error, tid={}", message.getTid(), e);
                } else {
                    console.warn("Client consume handle error", e);
                }
            }
        });
    }

    /**
     * 会话打开时
     */
    override onOpen(session: Session) {
        super.onOpen(session);

        console.info("Client session opened, sessionId={}", session.sessionId());

        if (this._client._subscriptionMap.size == 0) {
            return;
        }

        //用于重连时重新订阅
        let subscribeData = new Map<String, Set<String>>;
        for (let subscription of this._client._subscriptionMap.values()) {
            let queueNameSet = subscribeData.get(subscription.getTopic());
            if (queueNameSet == null) {
                queueNameSet = new Set<string>;
                subscribeData.set(subscription.getTopic(), queueNameSet);
            }
            queueNameSet.add(subscription.getQueueName());
        }

        let json = JSON.stringify(subscribeData);
        let entity = SocketD.newEntity(json)
            .metaPut(MqConstants.MQ_META_BATCH, "1")
            .metaPut("@", MqConstants.BROKER_AT_SERVER);

        session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity).await();

        console.info("Client onOpen batch subscribe successfully, sessionId=" + session.sessionId());
    }

    /**
     * 会话关闭时
     */
    override onClose(session: Session) {
        super.onClose(session);

        console.info("Client session closed, sessionId=" + session.sessionId());
    }

    /**
     * 会话出错时
     */
    override onError(session: Session, error: Error) {
        super.onError(session, error);

        if (error instanceof SocketdAlarmException) {
            console.warn("Client error, sessionId=" + session.sessionId(), error);
        } else {
            console.warn("Client error, sessionId=" + session.sessionId(), error);
        }
    }
}