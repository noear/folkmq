import {MqClient} from "./client/MqClient";
import {MqMessage} from "./client/MqMessage";
import {MqClientDefault} from "./client/MqClientDefault";
import {MqRouter} from "./client/MqRouter";
import {IoFunction} from "@noear/socket.d/transport/core/Typealias";
import {MqMessageReceived} from "./client/MqMessageReceived";
import {MqAlarm} from "./client/MqAlarm";

export class FolkMQ {

    /**
     * 获取版本代号（用于控制元信息版本）
     */
    static versionCode(): number {
        return 2;
    }

    static versionCodeAsString(): string {
        return FolkMQ.versionCode().toString();
    }

    /**
     * 获取版本
     */
    static versionName(): string {
        return "1.2.3";
    }

    /**
     * 创建客户端
     */
    static createClient(serverUrls: string[] | string): MqClient {
        return new MqClientDefault(serverUrls);
    }

    static newRouter(mappingHandler: IoFunction<MqMessageReceived, string>): MqRouter {
        return new MqRouter(mappingHandler);
    }

    static newMessage(content: string): MqMessage {
        return new MqMessage(content);
    }

    static newAlarm(content: string): MqAlarm {
        return new MqAlarm(content);
    }
}