
### 1.0.8
* 调整 "持久化" 概念改为 "观查者" 概念
* 调整 MqPersistent 改为 MqWatcher
* 调整 MqPersistentSnapshot 改为 MqWatcherSnapshot

### 1.0.7
* 添加 后端管理功能（主要是查看）
* 添加 FolkMQ 主类

### 1.0.6
* 添加 ack 专属指令
* 优化 快照持久化
* 完善 持久化单测

### 1.0.5
* 添加 持久化快照功能
* 添加 取消订阅功能

### 1.0.4

* 修复 多主题多客户端交差订阅时，派发可能混乱的问题
* 添加 Qos0 消息质量支持
* 添加 基础用户单测
* 优化 订阅关系管理

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