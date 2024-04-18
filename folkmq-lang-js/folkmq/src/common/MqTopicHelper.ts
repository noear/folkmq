

/**
 * @author noear
 * @since 1.4
 */
export class MqTopicHelper {
    /**
     * 获取完整主题
     */
    static getFullTopic(namespace: string, topic: string): string {
        if (namespace) {
            return namespace + ":" + topic;
        } else {
            return topic;
        }
    }

    /**
     * 获取主题
     */
    static getTopic(fullTopic: string) {
        let idx = fullTopic.indexOf(":");
        if (idx > 0) {
            return fullTopic.substring(idx + 1);
        } else {
            return fullTopic;
        }
    }
}