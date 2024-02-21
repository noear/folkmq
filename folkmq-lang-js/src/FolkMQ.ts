import {MqClient, MqClientDefault} from "./client/MqClient";
import {IMqMessage, MqMessage} from "./client/IMqMessage";

export class FolkMQ {
    /**
     * 获取版本
     */
    static version(): string {
        return "1.2.0";
    }

    /**
     * 创建客户端
     */
    static createClient(serverUrls: string[]): MqClient {
        return new MqClientDefault(serverUrls);
    }

    static newMessage(content: string): IMqMessage {
        return new MqMessage(content);
    }
}