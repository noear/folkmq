
### MqClient 接口

```java
/**
 * 消息客户端
 *
 * @author noear
 * @since 1.0
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
     *
     * @param auto 自动（默认为 true）
     */
    MqClient autoAcknowledge(boolean auto);

    /**
     * 发布重试
     *
     * @param times 次数（默认为 0）
     * */
    MqClient publishRetryTimes(int times);

    /**
     * 订阅主题
     *
     * @param topic           主题
     * @param consumer        消费者（实例 ip 或 集群 name）
     * @param consumerHandler 消费处理
     */
    void subscribe(String topic, String consumer, MqConsumeHandler consumerHandler) throws IOException;

    /**
     * 取消订阅主题
     *
     * @param topic    主题
     * @param consumer 消费者（实例 ip 或 集群 name）
     */
    void unsubscribe(String topic, String consumer) throws IOException;

    /**
     * 发布消息
     *
     * @param topic     主题
     * @param message   消息
     */
    CompletableFuture<?> publish(String topic, IMqMessage message) throws IOException;
}
```

### IMqMessage 接口

```java
/**
 * 消息接口
 */
public interface IMqMessage {
    /**
     * 跟踪ID
     */
    String getTid();

    /**
     * 内容
     */
    String getContent();

    /**
     * 定时时间
     */
    Date getScheduled();

    /**
     * 质量等级（0 或 1）
     */
    int getQos();
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
     * 
     * @param message 收到的消息
     */
    void consume(MqMessage message) throws IOException;
}
```

### MqMessage 接口

```java
/**
 * 收到的消息接口
 */
public interface IMqMessageReceived extends IMqMessage {
    /**
     * 主题
     */
    String getTopic();

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