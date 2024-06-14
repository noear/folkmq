import {ClusterClientSession} from "@noear/socket.d/cluster/ClusterClientSession";
import {MqClientListener} from "./MqClientListener";
import {IoConsumer} from "@noear/socket.d/transport/core/Typealias";
import {ClientConfig} from "@noear/socket.d/transport/client/ClientConfig";
import {MqSubscription} from "./MqSubscription";
import {SocketD} from "@noear/socket.d";
import {MqConstants} from "../common/MqConstants";
import {MqMessageReceived, MqMessageReceivedImpl} from "./MqMessageReceived";
import {MqMessage} from "./MqMessage";
import {SocketDConnectionException, SocketDException} from "@noear/socket.d/exception/SocketDException";
import {MqUtils} from "../common/MqUtils";
import {Session} from "@noear/socket.d/transport/core/Session";
import {MqClient, MqClientInternal} from "./MqClient";
import {FolkMQ} from "../FolkMQ";
import {FolkmqException} from "../exception/FolkmqException";
import {MqAssert} from "../common/MqAssert";
import {RequestStream} from "@noear/socket.d/transport/stream/Stream";
import {MqMetasV2} from "../common/MqMetasV2";
import {MqTransaction, MqTransactionImpl} from "./MqTransaction";
import {Entity} from "@noear/socket.d/transport/core/Entity";
import {MqAlarm} from "./MqAlarm";
import {MqTopicHelper} from "../common/MqTopicHelper";
import {EntityMetas} from "@noear/socket.d/transport/core/EntityMetas";

/**
 * 消息客户端默认实现
 *
 * @author noear
 * @since 1.0
 * @since 1.2
 */
export class MqClientDefault implements MqClientInternal {
    //事务回查
    public _transactionCheckback: IoConsumer<MqMessageReceived>;
    //监听处理
    public _listenHandler: IoConsumer<MqMessageReceived>;

    //服务端地址
    private _urls: Array<string>;
    //客户端会话
    private _clientSession: ClusterClientSession;
    //客户端监听
    private _clientListener: MqClientListener;
    //客户端配置
    private _clientConfigHandler: IoConsumer<ClientConfig>;
    //订阅字典
    private _subscriptionMap: Map<String, MqSubscription> = new Map<String, MqSubscription>();
    //客户端名字
    private _name: string;
    //命名空间
    private _namespace: string;

    //自动回执
    private _autoAcknowledge: boolean = true;

    constructor(urls: string[] | string, clientListener?: MqClientListener) {
        if (urls instanceof Array) {
            this._urls = urls;
        } else {
            this._urls = [urls];
        }

        if (clientListener) {
            this._clientListener = clientListener;
        } else {
            this._clientListener = new MqClientListener();
        }

        this._clientListener.init(this);
    }

    name(): string {
        return this._name;
    }

    nameAs(name: string): MqClient {
        this._name = name;
        return this;
    }

    namespace(): string {
        return this._namespace;
    }

    namespaceAs(namespace: string): MqClient {
        this._namespace = namespace;
        return this;
    }

    async connect(): Promise<MqClient> {
        let serverUrls = new Array<string>()

        for (let url of this._urls) {
            url = url.replace("folkmq:ws://", "sd:ws://");
            url = url.replace("folkmq:wss://", "sd:wss://");
            url = url.replace("folkmq://", "sd:tcp://");


            for (let url1 of url.split(",")) {
                if (this._name) {
                    if (url1.includes("?")) {
                        url1 = url1 + "&@=" + this._name;
                    } else {
                        url1 = url1 + "?@=" + this._name;
                    }
                }

                serverUrls.push(url1);
            }
        }

        this._clientSession = (await SocketD.createClusterClient(serverUrls)
            .config(c => {
                c.metaPut(MqConstants.FOLKMQ_VERSION, FolkMQ.versionCodeAsString())
                    .heartbeatInterval(6_000)
                    .ioThreads(1)
                    .codecThreads(1)
                    .exchangeThreads(1);

                if (this._namespace) {
                    c.metaPut(MqConstants.FOLKMQ_NAMESPACE, this._namespace)
                }

                if (this._clientConfigHandler) {
                    this._clientConfigHandler(c);
                }
            })
            .listen(this._clientListener)
            .open()) as ClusterClientSession;

        return this;
    }

    disconnect() {
        this._clientSession.close();
    }

    config(configHandler: IoConsumer<ClientConfig>): MqClient {
        this._clientConfigHandler = configHandler;
        return this;
    }

    /**
     * 自动回执
     */
    autoAcknowledge(auto: boolean): MqClient {
        this._autoAcknowledge = auto;
        return this;
    }

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumerGroup   消费者组
     * @param consumerHandler 消费处理
     */
    subscribe(topic: string, consumerGroup: string | null, autoAck: boolean | null, consumerHandler: IoConsumer<MqMessageReceived>) {
        if (consumerGroup == null) {
            consumerGroup = this.name();
        }

        if (autoAck == null) {
            autoAck = this._autoAcknowledge;
        }

        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(consumerGroup, "Param 'consumerGroup' can't be null");
        MqAssert.requireNonNull(consumerHandler, "Param 'consumerHandler' can't be null");

        MqAssert.assertMeta(topic, "topic");
        MqAssert.assertMeta(consumerGroup, "consumerGroup");

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(this._namespace, topic);


        let subscription = new MqSubscription(topic, consumerGroup, autoAck, consumerHandler);

        this._subscriptionMap.set(subscription.getQueueName(), subscription);

        if (this._clientSession != null) {
            for (let session of this._clientSession.getSessionAll()) {
                //如果有连接会话，则执行订阅
                let entity = SocketD.newEntity("")
                    .metaPut(MqConstants.MQ_META_TOPIC, subscription.getTopic())
                    .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, subscription.getConsumerGroup())
                    .metaPut(EntityMetas.META_X_UNLIMITED, "1")
                    .at(MqConstants.PROXY_AT_BROKER_ALL);

                //使用 Qos1
                session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity, 30_000).await();

                console.info(`Client subscribe successfully: ${topic}#${consumerGroup}, sessionId=${session.sessionId()}`);
            }
        }
    }

    unsubscribe(topic: string, consumerGroup: string | null) {
        if (consumerGroup == null) {
            consumerGroup = this.name();
        }

        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(consumerGroup, "Param 'consumerGroup' can't be null");

        MqAssert.assertMeta(topic, "topic");
        MqAssert.assertMeta(consumerGroup, "consumerGroup");

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(this._namespace, topic);


        let queueName = topic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        this._subscriptionMap.delete(queueName);

        if (this._clientSession != null) {
            for (let session of this._clientSession.getSessionAll()) {
                //如果有连接会话
                let entity = SocketD.newEntity("")
                    .metaPut(MqConstants.MQ_META_TOPIC, topic)
                    .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup)
                    .at(MqConstants.PROXY_AT_BROKER_ALL);

                //使用 Qos1
                session.sendAndRequest(MqConstants.MQ_EVENT_UNSUBSCRIBE, entity, 30_000).await();

                console.info(`Client unsubscribe successfully: ${topic}#${consumerGroup}， sessionId=${session.sessionId()}`);
            }
        }
    }

    async publish(topic: string, message: MqMessage) {
        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(message, "Param 'message' can't be null");

        MqAssert.assertMeta(topic, "topic");

        if (this._clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(this._namespace, topic);

        let session = this._clientSession.getSessionAny(this.diversionOrNull(topic, message));
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        let entity = MqUtils.getOf(session as Session).publishEntityBuild(topic, message);

        if (message.getQos() > 0) {
            //::Qos1
            let resp = await session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH, entity).await();

            let confirm = parseInt(resp.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
            if (confirm != 1) {
                let messsage = "Client message publish confirm failed: " + resp.dataAsString();
                throw new FolkmqException(messsage);
            }
        } else {
            //::Qos0
            session.send(MqConstants.MQ_EVENT_PUBLISH, entity);
        }
    }

    async unpublish(topic: string, key: string) {
        MqAssert.requireNonNull(topic, "Param 'topic' can't be null");
        MqAssert.requireNonNull(key, "Param 'key' can't be null");

        MqAssert.assertMeta(topic, "topic");
        MqAssert.assertMeta(key, "key");

        if (this._clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        //支持命名空间
        topic = MqTopicHelper.getFullTopic(this._namespace, topic);

        let session = this._clientSession.getSessionAny(null);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        let entity = SocketD.newEntity("")
            .metaPut(MqConstants.MQ_META_TOPIC, topic)
            .metaPut(MqConstants.MQ_META_KEY, key)
            .at(MqConstants.PROXY_AT_BROKER_ALL);

        //::Qos1
        let resp = await session.sendAndRequest(MqConstants.MQ_EVENT_UNPUBLISH, entity).await();

        let confirm = parseInt(resp.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
        if (confirm != 1) {
            let messsage = "Client message unpublish confirm failed: " + resp.dataAsString();
            throw new FolkmqException(messsage);
        }
    }

    listen(listenHandler: IoConsumer<MqMessageReceived>) {
        //检查必要条件
        if (!this._name) {
            throw new Error("Client 'name' can't be empty");
        }

        this._listenHandler = listenHandler;
    }

    send(message: MqMessage, toName: string, timeout?: number): RequestStream | null {
        //检查必要条件
        if (!this._name) {
            throw new Error("Client 'name' can't be empty");
        }

        //检查参数
        MqAssert.requireNonNull(toName, "Param 'toName' can't be null");
        MqAssert.requireNonNull(message, "Param 'message' can't be null");

        MqAssert.assertMeta(toName, "toName");

        if (this._clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        let session = this._clientSession.getSessionAny(null);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        message.internalSender(this.name());
        let entity = MqUtils.getOf(session as Session).publishEntityBuild("", message);
        entity.putMeta(MqMetasV2.MQ_META_CONSUMER_GROUP, toName);
        entity.at(toName);

        if (message.getQos() > 0) {
            //::Qos1
            return session.sendAndRequest(MqConstants.MQ_EVENT_REQUEST, entity, timeout);
        } else {
            //::Qos0
            session.send(MqConstants.MQ_EVENT_REQUEST, entity);
            return null;
        }
    }

    transactionCheckback(transactionCheckback: IoConsumer<MqMessageReceived>): MqClient {
        if (transactionCheckback != null) {
            this._transactionCheckback = transactionCheckback;
        }

        return this;

    }

    newTransaction(): MqTransaction {
        //检查必要条件
        //检查必要条件
        if (!this._name) {
            throw new Error("Client 'name' can't be empty");
        }

        return new MqTransactionImpl(this);
    }

    /**
     * 发布二次提交
     *
     * @param tmid       事务管理id
     * @param keyAry     消息主键集合
     * @param isRollback 是否回滚
     */
    async publish2(tmid: string, keyAry: string[], isRollback: boolean) {
        if (keyAry == null || keyAry.length == 0) {
            return;
        }

        if (this._clientSession == null) {
            throw new SocketDConnectionException("Not connected!");
        }

        let session = this._clientSession.getSessionAny(tmid);
        if (session == null || session.isValid() == false) {
            throw new SocketDException("No session is available!");
        }

        let entity = SocketD.newEntity(keyAry.join(","))
            .metaPut(MqConstants.MQ_META_ROLLBACK, (isRollback ? "1" : "0"))
            .at(MqConstants.PROXY_AT_BROKER_HASH); //事务走哈希

        //::Qos1
        let resp = await session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH2, entity).await();

        let confirm = parseInt(resp.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
        if (confirm != 1) {
            let messsage = "Client message publish2 confirm failed: " + resp.dataAsString();
            throw new FolkmqException(messsage);
        }
    }

    /**
     * 消费答复
     *
     * @param session 会话
     * @param from    来源消息
     * @param message 收到的消息
     * @param isOk    回执
     * @param entity  实体
     */
    reply(session: Session, message: MqMessageReceivedImpl, isOk: boolean, entity: Entity | null) {
        //确保只答复一次
        if (message.isReplied()) {
            //已答复
            return;
        } else {
            //置为答复
            message.setReplied(true);
        }

        //发送“回执”，向服务端反馈消费情况
        if (message.getQos() > 0) {
            if (session.isValid()) {
                if (entity == null) {
                    entity = SocketD.newEntity();
                }

                entity.putMeta(MqMetasV2.MQ_META_VID, FolkMQ.versionCodeAsString());
                entity.putMeta(MqMetasV2.MQ_META_TOPIC, message.getFullTopic());
                entity.putMeta(MqMetasV2.MQ_META_CONSUMER_GROUP, message.getConsumerGroup());
                entity.putMeta(MqMetasV2.MQ_META_KEY, message.getKey());

                if (entity instanceof MqAlarm) {
                    session.sendAlarm(message.getSource(), entity.dataAsString());
                } else {
                    entity.putMeta(MqConstants.MQ_META_ACK, isOk ? "1" : "0");
                    session.replyEnd(message.getSource(), entity);
                }
            }
        }
    }

    protected diversionOrNull(fullTopic: string, message: MqMessage): string | null {
        if (message.isTransaction()) {
            return message.getTmid();
        } else if (message.isSequence()) {
            if (message.getSequenceSharding()) {
                return message.getSequenceSharding();
            } else {
                return fullTopic;
            }
        } else {
            return null;
        }
    }

    public getSubscription(fullTopic: string, consumerGroup: string) {
        let queueName = fullTopic + MqConstants.SEPARATOR_TOPIC_CONSUMER_GROUP + consumerGroup;
        return this._subscriptionMap.get(queueName);

    }

    public getSubscriptionAll(): Set<MqSubscription> {
        return new Set<MqSubscription>(this._subscriptionMap.values());
    }

    public getSubscriptionSize(): number {
        return this._subscriptionMap.size;
    }
}