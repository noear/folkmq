import {MqMessageReceived} from "./IMqMessage";


export interface MqConsumeHandler {
    /**
     * 消费
     *
     * @param message 收到的消息
     */
    consume(message: MqMessageReceived);
}