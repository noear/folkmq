import {StringEntity} from "@noear/socket.d/transport/core/Entity";

export class MqAlarm extends StringEntity {
    constructor(data: string) {
        super(data);
    }
}