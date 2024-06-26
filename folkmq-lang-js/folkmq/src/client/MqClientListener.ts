import {EventListener} from "@noear/socket.d/transport/core/listener/EventListener";
import {MqConstants} from "../common/MqConstants";
import {Session} from "@noear/socket.d/transport/core/Session";
import {SocketD} from "@noear/socket.d";
import {SocketDAlarmException} from "@noear/socket.d/exception/SocketDException";
import {MqClientDefault} from "./MqClientDefault";
import {MqMessageReceivedImpl} from "./MqMessageReceived";
import {Message} from "@noear/socket.d/transport/core/Message";
import {EntityMetas} from "@noear/socket.d/transport/core/EntityMetas";
import {MqAlarm} from "./MqAlarm";

export class MqClientListener extends EventListener {
    private _client: MqClientDefault;

    /**
     * 初始化
     * */
     init(client: MqClientDefault) {
        this._client = client;

        //接收派发指令
        this.doOn(MqConstants.MQ_EVENT_DISTRIBUTE, (s, m) => {
            let message = new MqMessageReceivedImpl(client, s, m);

            try {
                this.onReceive(s, m, message, false);
            } catch (e) {
                console.warn("Client consume handle error, sid=" + m.sid(), e);
                client.reply(s, message, false, new MqAlarm(String(e)));
            }
        });

        this.doOn(MqConstants.MQ_EVENT_REQUEST, (s, m) => {
            let message = new MqMessageReceivedImpl(client, s, m);

            try {
                this.onReceive(s, m, message, true);
            } catch (e) {
                console.warn("Client consume handle error, sid=" + m.sid(), e);
                client.reply(s, message, false, new MqAlarm(String(e)));
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
                    console.warn("Client request handle error, key=" + message.getKey(), err);
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
                        this._client.reply(s, message, true, null);
                    }
                } else {
                    //没有订阅
                    this._client.reply(s, message, false, null);
                }
            } catch (e) {
                try {
                    if (subscription != null) {
                        //有订阅
                        if (subscription.isAutoAck()) {
                            this._client.reply(s, message, false, null);
                        }
                    } else {
                        //没有订阅
                        this._client.reply(s, message, false, null);
                    }

                    console.warn("Client consume handle error, key=" + message.getKey(), e);
                } catch (err) {
                    console.warn("Client consume handle error, key=" + message.getKey(), err);
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
        let subscribeData = {};
        for (let subscription of this._client.getSubscriptionAll()) {
            let queueNameSet:Array<string> = subscribeData[subscription.getTopic()];
            if (queueNameSet == null) {
                queueNameSet = [];
                subscribeData[subscription.getTopic()] = queueNameSet;
            }
            queueNameSet.push(subscription.getQueueName());
        }

        let json = JSON.stringify(subscribeData);
        let entity = SocketD.newEntity(json)
            .metaPut(MqConstants.MQ_META_BATCH, "1")
            .metaPut(EntityMetas.META_X_UNLIMITED, "1")
            .metaPut("@", MqConstants.PROXY_AT_BROKER);

        session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity, 30_000).await();

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