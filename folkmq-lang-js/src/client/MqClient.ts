import {ClientConfig} from "@noear/socket.d/transport/client/ClientConfig";
import {IoConsumer} from "@noear/socket.d/transport/core/Typealias";
import {Session} from "@noear/socket.d/transport/core/Session";
import {Message} from "@noear/socket.d/transport/core/Message";
import {IMqMessage, MqMessageReceived, MqMessageReceivedImpl} from "./IMqMessage";
import {MqClientListener} from "./MqClientListener";
import {ClusterClientSession} from "@noear/socket.d/cluster/ClusterClientSession";
import {SocketD} from "@noear/socket.d";
import {MqConstants} from "../common/MqConstants";
import {MqSubscription} from "./MqSubscription";
import {SocketdConnectionException, SocketdException} from "@noear/socket.d/exception/SocketdException";
import {MqUtils} from "../common/MqUtils";


export interface MqClient {
    /**
     * 连接
     */
    connect(): Promise<MqClient>;

    /**
     * 断开连接
     */
    disconnect();

    /**
     * 配置
     * */
    config(configHandler: IoConsumer<ClientConfig>): MqClient;

    /**
     * 客户端配置
     */

    //config( configHandler: ClientConfigHandler):MqClient;

    /**
     * 自动回执
     *
     * @param auto 自动（默认为 true）
     */
    autoAcknowledge(auto: boolean): MqClient;

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumerGroup   消费者组
     * @param consumerHandler 消费处理
     */
    subscribe(topic: string, consumerGroup: string, consumerHandler: IoConsumer<MqMessageReceived>);

    /**
     * 取消订阅主题
     *
     * @param topic         主题
     * @param consumerGroup 消费者组
     */
    unsubscribe(topic: string, consumerGroup: string);

    /**
     * 同步发布消息
     *
     * @param topic   主题
     * @param message 消息
     */
    publish(topic: string, message: IMqMessage);

    /**
     * 取消发布
     *
     * @param topic 主题
     * @param tid   事务id
     */
    unpublish(topic: string, tid: string);
}

export interface MqClientInternal extends MqClient{
    /**
     * 消费回执
     *
     * @param session 会话
     * @param from    来源消息
     * @param message 收到的消息
     * @param isOk    回执
     */
    acknowledge(session: Session, from: Message, message: MqMessageReceivedImpl, isOk: boolean);
}


export class MqClientDefault implements MqClientInternal {
    //服务端地址
    private _serverUrls: Array<string>;
    //客户端会话
    private _clientSession: ClusterClientSession;
    //客户端监听
    private _clientListener: MqClientListener;
    //客户端配置
    private _clientConfigHandler: IoConsumer<ClientConfig>;
    //订阅字典
    public _subscriptionMap: Map<String, MqSubscription>;

    //自动回执
    public _autoAcknowledge: boolean = true;

    constructor(urls: Array<string>) {
        this._serverUrls = new Array<string>();
        this._subscriptionMap = new Map<String, MqSubscription>();
        this._clientListener = new MqClientListener(this);

        for (let url of urls) {
            url = url.replace("folkmq:ws://", "sd:ws://");
            url = url.replace("folkmq://", "sd:tcp://");
            this._serverUrls.push(url);
        }
    }

    async connect(): Promise<MqClient> {
        this._clientSession = await SocketD.createClusterClient(this._serverUrls)
            .config(c => c.fragmentSize(MqConstants.MAX_FRAGMENT_SIZE))
            .config(this._clientConfigHandler)
            .listen(this._clientListener)
            .open();

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
    subscribe(topic: string, consumerGroup: string, consumerHandler: IoConsumer<MqMessageReceived>) {
        let subscription = new MqSubscription(topic, consumerGroup, consumerHandler);

        this._subscriptionMap.set(topic, subscription);

        if (this._clientSession != null) {
            for (let session of this._clientSession.getSessionAll()) {
                //如果有连接会话，则执行订阅
                let entity = SocketD.newEntity("")
                    .metaPut(MqConstants.MQ_META_TOPIC, subscription.getTopic())
                    .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, subscription.getConsumerGroup())
                    .at(MqConstants.BROKER_AT_SERVER_ALL);

                //使用 Qos1
                session.sendAndRequest(MqConstants.MQ_EVENT_SUBSCRIBE, entity).await();

                console.info(`Client subscribe successfully: ${topic}#${consumerGroup}, sessionId=${session.sessionId()}`);
            }
        }
    }

    unsubscribe(topic: string, consumerGroup: string) {
        this._subscriptionMap.delete(topic);

        if (this._clientSession != null) {
            for (let session of this._clientSession.getSessionAll()) {
                //如果有连接会话
                let entity = SocketD.newEntity("")
                    .metaPut(MqConstants.MQ_META_TOPIC, topic)
                    .metaPut(MqConstants.MQ_META_CONSUMER_GROUP, consumerGroup)
                    .at(MqConstants.BROKER_AT_SERVER_ALL);

                //使用 Qos1
                session.sendAndRequest(MqConstants.MQ_EVENT_UNSUBSCRIBE, entity).await();

                console.info(`Client unsubscribe successfully: ${topic}#${consumerGroup}， sessionId=${session.sessionId()}`);
            }
        }
    }

    async publish(topic: string, message: IMqMessage) {
        if (this._clientSession == null) {
            throw new SocketdConnectionException("Not connected!");
        }

        let session = this._clientSession.getSessionOne();
        if (session == null || session.isValid() == false) {
            throw new SocketdException("No session is available!");
        }

        let entity = MqUtils.publishEntityBuild(topic, message);

        if (message.getQos() > 0) {
            //::Qos1
            let resp = await session.sendAndRequest(MqConstants.MQ_EVENT_PUBLISH, entity).await();

            let confirm = parseInt(resp.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
            if (confirm != 1) {
                let messsage = "Client message publish confirm failed: " + resp.dataAsString();
                throw new SocketdException(messsage);//throw new FolkmqException(messsage);
            }
        } else {
            //::Qos0
            session.send(MqConstants.MQ_EVENT_PUBLISH, entity);
        }
    }

    async unpublish(topic: string, tid: string) {
        if (this._clientSession == null) {
            throw new SocketdConnectionException("Not connected!");
        }

        let session = this._clientSession.getSessionOne();
        if (session == null || session.isValid() == false) {
            throw new SocketdException("No session is available!");
        }

        let entity = SocketD.newEntity("")
            .metaPut(MqConstants.MQ_META_TOPIC, topic)
            .metaPut(MqConstants.MQ_META_TID, tid)
            .metaPut("@", MqConstants.BROKER_AT_SERVER_ALL);

        //::Qos1
        let resp = await session.sendAndRequest(MqConstants.MQ_EVENT_UNPUBLISH, entity).await();

        let confirm = parseInt(resp.metaOrDefault(MqConstants.MQ_META_CONFIRM, "0"));
        if (confirm != 1) {
            let messsage = "Client message unpublish confirm failed: " + resp.dataAsString();
            throw new SocketdException(messsage);//throw new FolkmqException(messsage);
        }
    }


    /**
     * 消费回执
     *
     * @param message 收到的消息
     * @param isOk    回执
     */
    acknowledge(session: Session, from: Message, message: MqMessageReceivedImpl, isOk: boolean) {
        //发送“回执”，向服务端反馈消费情况
        if (message.getQos() > 0) {
            if (session.isValid()) {
                session.replyEnd(from, SocketD.newEntity("")
                    .metaPut(MqConstants.MQ_META_ACK, isOk ? "1" : "0"));
            }
        }
    }

    /**
     * 关闭
     */
    close() {
        this._clientSession.close();
    }
}