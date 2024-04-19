import {EventListener} from "@noear/socket.d/transport/core/Listener";
import {MqConstants} from "../common/MqConstants";
import {Session} from "@noear/socket.d/transport/core/Session";
import {SocketD} from "@noear/socket.d";
import {SocketDAlarmException} from "@noear/socket.d/exception/SocketDException";
import {MqClientDefault} from "./MqClientDefault";
import {MqMessageReceivedImpl} from "./MqMessageReceived";
import {Message} from "@noear/socket.d/transport/core/Message";

export class MqClientListener extends EventListener {
    private _client: MqClientDefault;

    /**
     * 初始化
     * */
     init(client: MqClientDefault) {
        this._client = client;

        //接收派发指令
        this.doOn(MqConstants.MQ_EVENT_DISTRIBUTE, (s, m) => {
            try {
                let message = new MqMessageReceivedImpl(client, s, m);
                this.onReceive(s, m, message, false);
            } catch (e) {
                console.warn("Client consume handle error, sid=" + m.sid(), e);
            }
        });

        this.doOn(MqConstants.MQ_EVENT_REQUEST, (s, m) => {
            try {
                let message = new MqMessageReceivedImpl(client, s, m);
                this.onReceive(s, m, message, true);
            } catch (e) {
                console.warn("Client consume handle error, sid=" + m.sid(), e);
            }
        });
    }

    /**
     * 接收时
     * */
    onReceive(s: Session, m: Message, message: MqMessageReceivedImpl, isRequest: boolean) {
        if (isRequest) {
            try {
                if (message.isTransaction()) {
                    if (this._client._transactionCheckback != null) {
                        this._client._transactionCheckback(message);
                    } else {
                        s.sendAlarm(m, "Client no checkback handler!");
                    }
                } else {
                    if (this._client._listenHandler != null) {
                        this._client._listenHandler(message);
                    } else {
                        s.sendAlarm(m, "Client no request handler!");
                    }
                }
            } catch (e) {
                try {
                    if (s.isValid()) {
                        s.sendAlarm(m, "Client request handle error:" + e);
                    }
                    console.warn("Client request handle error, key=" + message.getKey(), e);
                } catch (err) {
                    console.warn("Client request handle error, key=" + message.getKey(), e);
                }
            }
        } else {
            let subscription = this._client.getSubscription(message.getFullTopic(), message.getConsumerGroup());

            try {
                if (subscription != null) {
                    //有订阅
                    subscription.consume(message);

                    //是否自动回执
                    if (subscription.isAutoAck()) {
                        this._client.reply(s, m, message, true, null);
                    }
                } else {
                    //没有订阅
                    this._client.reply(s, m, message, false, null);
                }
            } catch (e) {
                try {
                    if (subscription != null) {
                        //有订阅
                        if (subscription.isAutoAck()) {
                            this._client.reply(s, m, message, false, null);
                        }
                    } else {
                        //没有订阅
                        this._client.reply(s, m, message, false, null);
                    }

                    console.warn("Client consume handle error, key=" + message.getKey(), e);
                } catch (err) {
                    console.warn("Client consume handle error, key=" + message.getKey(), e);
                }
            }
        }
    }

    /**
     * 会话打开时
     */
    override onOpen(session: Session) {
        super.onOpen(session);

        console.info("Client session opened, sessionId=" + session.sessionId());

        if (this._client.getSubscriptionSize() == 0) {
            return;
        }

        //用于重连时重新订阅
        let subscribeData = new Map<String, Set<String>>;
        for (let subscription of this._client.getSubscriptionAll()) {
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

        if (error instanceof SocketDAlarmException) {
            console.warn("Client error, sessionId=" + session.sessionId(), error);
        } else {
            console.warn("Client error, sessionId=" + session.sessionId(), error);
        }
    }
}