
### 1.0.3

* 添加 MqPersistent (持久化接口)支持
* 优化 ack 保障策略

### 1.0.2
* 调整 delayedFuture:cancel 为 false（异步发时，避免会关掉通道）
* 调整 规范化异步类型
* 简化 MqConsumerQueue 的实现方式（改用延时队列）
* 优化 日志打印与变量命名

### 1.0.0-M3

* user 概念改为 comsumer 概念
* 添加 自恢复 能力（如果因某些原因通道出错或关闭，客户端重新连接并重新订阅，最终完成派发）
* 优化注释与变量命名

### 1.0.0-M2

* identity 概念改为 user 概念
* 发消息时添加 scheduled 属性，支持发定时消息
* 将消费者和订阅者接口合到 MqClient，减少文件
* Subscription 更名 MqSubscription
* 简化派发方式