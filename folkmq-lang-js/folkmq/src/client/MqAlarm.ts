import {StringEntity} from "@noear/socket.d/transport/core/entity/StringEntity";

export class MqAlarm extends StringEntity {
    constructor(data: string) {
        super(data);
    }
}