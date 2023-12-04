
### 1.0.14
* 拆分 client::publish 为 publish + publishAsync

### 1.0.13
* 添加 客户端发布重试 MqClient::publishRetryTimes
* 调整 客户端发布接口为1个（原来4个），原多参数改为1个实体参数（未来可能还会扩充属性）
* socket.d 升为 2.1.2

### 1.0.12
* 添加 folkmq-server 管理后台会话保持（重启服务不会被退出）
* 添加 folkmq-server 对 prometheus 支持
* 添加 folkmq-server 延时消息的统计看板
* 优化 快照方案（更稳、更快、更省内存）
* 优化 线程安全细节
* 完成 持久化测试延时不同级别消息（如：l2,l3），准确恢复
* socket.d 升为 2.1.1

### 1.0.10
* 完成 Socket.D Broker 集群模式!
* socket.d 升为 2.1.0

### 1.0.9
* 添加 folkmq-transport-java-tcp 模块
* 添加 folkmq-transport-netty 模块
* 添加 folkmq-transport-smartsocket 模块
* 完善 快照策略（借鉴 redis 的策略）
* 完善 快照压缩处理
* socket.d 升为 2.0.24

### 1.0.8
* 调整 "持久化" 概念改为 "观查者" 概念（更通用，后面要做监视）
* 调整 MqPersistent 改为 MqWatcher
* 调整 MqPersistentSnapshot 改为 MqWatcherSnapshot
* 调整 MqConsumerHandler 改为 MqConsumeHandler
* 添加 MqWatcherMetrics 做数据监视
* 恢复 由 sendAndSubscribe 实现 ACK（跑分更高点）
* snack3 升为 3.2.84
* socket.d 升为 2.0.22

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