
### MqClient 接口

```java
/**
 * 消息客户端
 */
public interface MqClient {
    /**
     * 连接
     */
    MqClient connect() throws IOException;

    /**
     * 断开连接
     */
    void disconnect() throws IOException;

    /**
     * 客户端配置
     */
    MqClient config(ClientConfigHandler configHandler);

    /**
     * 自动回执
     */
    MqClient autoAcknowledge(boolean auto);

    /**
     * 订阅主题
     */
    void subscribe(String topic, String consumer, MqConsumeHandler consumerHandler) throws IOException;

    /**
     * 取消订阅主题
     */
    void unsubscribe(String topic, String consumer) throws IOException;

    /**
     * 发布消息
     */
    default CompletableFuture<?> publish(String topic, String content) throws IOException {
        return publish(topic, content, null, 1);
    }

    /**
     * 发布消息
     */
    default CompletableFuture<?> publish(String topic, String content, int qos) throws IOException {
        return publish(topic, content, null, qos);
    }

    /**
     * 发布消息
     */
    default CompletableFuture<?> publish(String topic, String content, Date scheduled) throws IOException {
        return publish(topic, content, scheduled, 1);
    }

    /**
     * 发布消息
     */
    CompletableFuture<?> publish(String topic, String content, Date scheduled, int qos) throws IOException;
}
```

### MqConsumeHandler 接口

```java

/**
 * 消费处理器
 */
public interface MqConsumeHandler {
    /**
     * 消费
     */
    void consume(MqMessage message) throws IOException;
}

```

### MqMessage 接口

```java
/**
 * 消息结构体定义
 */
public interface MqMessage {
    /**
     * 事务ID
     */
    String getTid();

    /**
     * 主题
     */
    String getTopic();

    /**
     * 内容
     */
    String getContent();

    /**
     * 质量等级（0 或 1）
     */
    int getQos();

    /**
     * 已派发次数
     */
    int getTimes();

    /**
     * 回执
     */
    void acknowledge(boolean isOk) throws IOException;
}
```