import {MqClient} from "./client/MqClient";
import {MqMessage} from "./client/MqMessage";
import {MqClientDefault} from "./client/MqClientDefault";
import {MqRouter} from "./client/MqRouter";
import {IoFunction} from "@noear/socket.d/transport/core/Typealias";
import {MqMessageReceived} from "./client/MqMessageReceived";
import {MqAlarm} from "./client/MqAlarm";
import {Entity} from "@noear/socket.d/transport/core/Entity";
import {SocketD} from "@noear/socket.d";

export class FolkMQ {

    /**
     * 获取版本代号（用于控制元信息版本）
     */
    static versionCode(): number {
        return 2;
    }

    /**
     * 获取版本代号字符串形式
     */
    static versionCodeAsString(): string {
        return FolkMQ.versionCode().toString();
    }

    /**
     * 获取版本
     */
    static versionName(): string {
        return "1.4.1";
    }

    /**
     * 创建客户端
     */
    static createClient(serverUrls: string[] | string): MqClient {
        return new MqClientDefault(serverUrls);
    }

    /**
     * 新建路由
     * */
    static newRouter(mappingHandler: IoFunction<MqMessageReceived, string>): MqRouter {
        return new MqRouter(mappingHandler);
    }

    /**
     * 新建消息
     * */
    static newMessage(content: string | ArrayBuffer): MqMessage {
        return new MqMessage(content);
    }

    /**
     * 新建告警
     * */
    static newAlarm(content: string): MqAlarm {
        return new MqAlarm(content);
    }

    /**
     * 新建实体
     * */
    static newEntity(data?: string | Blob | ArrayBuffer): Entity {
        return SocketD.newEntity(data);
    }
}