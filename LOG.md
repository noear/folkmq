规划特性

* 添加 命名空间与多租户支持（企业版）
* 添加 后台资源看板（显示内存），并添加手动 GC 操作?
* 添加 broker 配置可同步给 server 的能力

### 1.7.11
* 添加 新的持久化接口定义（并完成部分适配）
* solon 升为 3.1.0
* snack3 升为 3.2.129
* socket.d 升为 2.5.18

### 1.7.10
* socket.d 升为 2.5.14
* solon 升为 3.0.5
* snack3 升为 3.2.122


### 1.7.9
* socket.d 升为 2.5.13
* solon 升为 3.0.3
* snack3 升为 3.2.120

### 1.7.8
* socket.d 升为 2.5.12
* 添加 folkmq-borker wrapPort,wrapHost 配置支持（集群连接时，通过 port, host 元信息传递）

### 1.7.5
* socket.d 升为 2.5.9

### 1.7.4
* 增加 broker-embedded 支持 context-path 

### 1.7.3
* 修复 ttl消息，在没消费者时会自动延时的问题

### 1.7.2
* 调整 broker-embedded 默认改为 smartsocket 适配
* socket.d 升为 2.5.7

### 1.7.1
* 优化 MqBorkerListener “新确认模式”在单机下，多种客户端版本混用的兼容处理
* 增加 MqClientDefault 订阅时的连接状态检测，如果未连接则转为 onOpen 处理
* 调整 禁止“:”做为主题、消费组等字符


### 1.7.0

* 工件更名（协议与接口向下兼容 ）

| 旧名                   | 新名                     | 备注        |
|----------------------|------------------------|-----------|
| folkmq-embedded      | folkmq-broker-embedded | 内嵌版       |
| folkmq-server        | folkmq-broker          | 单机版       |
| folkmq-server-broker | folkmq-proxy           | 集群版（代理模式） |


* 添加 folkmq-broker-embedded 传输接口配置支持
* 优化 folkmq-proxy 队列看板数字获取方式
* socket.d 升为 2.5.5

### 1.6.0
* 新增 控制台 “流量看板”
* 新增 客户端流量控制支持（trafficLimiter），可控制客户端内存占用
* 优化 folkmq-server 启用新的确认机制（向下兼容），可减少内存占用
* 修复 python sdk 批量订阅无效的问题
* 修复 javascript sdk 批量订阅无效的问题
* socket.d 升为 2.5.4

### 1.5.3
* 添加 server-broker 控制多账账号管理??
* 修复 内嵌版登录鉴权跳错相对地址的问题

### 1.5.2
* 新增 folkmq-embedded （带控制台的"内嵌版"）
* 添加 server-broker 的 folkmq.maxConsumeWaiting 配置支持
* 优化 server-broker 许可证配置改为可视界面
* 优化 强制派送条件，对正在派发中或超过1次的消息有效（之前为2次）
* 优化 强制派空处理
* socket.d 升为 2.5.3

### 1.5.1
* 优化 消息事务增加对“延时消息”、“时效消息”支持
* 修复 "Qos0消息"、"广播消息" 失效的问题（1.5.0 出现的）

### 1.5.0
* 新增 广播消息
* 添加 后台强制清空 操作
* 添加 "fokmq:ws" 适配 websocket 子协议验证（避免乱连）
* 添加 "server-broker" 后台集群节点（内存用率）
* 优化 异步消息发送端的内存控制
* 优化 客户端锁处理（无锁改为顺序锁）
* sokcet.d 升为 2.5.1

### 1.4.6
* 添加 基于内存的限流支持客户端
* 修复 动态计数失真的问题
* 优化 后台发送消息时，如果没有主题。提示失败

### 1.4.5
* 添加 基于内存的限流（服务端）

### 1.4.4
* 增加 `folkmq:wss://` 协议头支持
* 增加 与小程序的兼容性
* sokcet.d 升为 2.4.16

### 1.4.3
* 新增 python client sdk
* 添加 客户端虚拟命名空间支持
* sokcet.d 升为 2.4.14

### 1.4.2
* 添加 自定义 tid(key) 支持（之前自动生成）
* 优化 Server 预关闭支持
* 优化 顺序消息的强制派发处理
* sokcet.d 升为 2.4.10

### 1.4.1
* 修复 客户端不能自动重连的问题

### 1.4.0
* 添加 PackagingLoopImpl 新的构造函数
* 添加 外部配置文件加载支持
* 添加 消息二进制数据支持
* 添加 顺序消息分支持（在集群下才有效）
* 优化 顺序消息没有消费者时 server 端 cpu 过高的问题
* 优化 企业版授权控制

### 1.3.2
* 调整 控制台发布消息后保持在原界面
* 调整 添加流转批小工具
* sokcet.d 升为 2.4.7

### 1.3.1
* 调整 docker 基础镜像改为：adoptopenjdk/openjdk11-openj9 （内存节约 1/2 左右）
* 调整 folkmq-broker 更名为：folkmq-server-broker （相互兼容，体验不变）
* 优化 顺序消息的消费逻辑（改为串行消费），更适合数据库有序同步之类的场景

### 1.3.0
* 完善 许可证本地处理机制（基于rsa签名机制）

### 1.2.4
* 添加 folkmq-server 对 ws 输传协议的支持

### 1.2.3
* 添加 rpc 异常传导机制
* 完成 javascript 语言客户端实现
* sokcet.d 升为 2.4.5

### 1.2.2
* 调整 response 拆分为：transactionCheckback 和 listen
* 调整 request 改为 send
* 添加 transactionCheckback 用于响应服务端的事务回查
* 添加 listen 和 send 配套接口
* 添加 后台图标
* 完善 许可证本地处理机制
* sokcet.d 升为 2.4.4

### 1.2.1
* 添加 协议版本的握手传递
* 添加 消息事务支持（即二段式提交），支持反向事务确认
* 添加 请求响应模式支持（即 rpc 模式）
* 添加 消息用户属性支持
* 优化 内存占用与快照大小
* 优化 安全停止延时改为4秒
* 优化 客户端相关参数校验
* 优化 客户端的心跳间隔为6秒
* 优化 停止打印信息
* sokcet.d 升为 2.4.3

### 1.1.0
* 调整 消息流处理改为单线程架构！
* 添加 “绝对顺序”消息支持（同时支持单机模式与集群模式）
* 修复 客户端问题：同一个项目内，用不同的 consumerGroup 订阅同一个topic 会被覆盖的问题
* sokcet.d 升为 2.4.0

### 1.0.32
* 添加 单机模式下绝对有序支持
* 添加 集群安全停止支持
* 添加 集成管理接口支持
* sokcet.d 升为 2.3.11

### 1.0.31
* sokcet.d 升为 2.3.8

### 1.0.30
* 修复 消息过期判断

### 1.0.29
* 添加 消息过期支持
* sokcet.d 升为 2.3.6
* solon 升为 2.6.5

```java
let msg = new MqMessage("hello").expiration(new Date(System.currentTimeMillis() + 5000));
client.publish("demo", msg);
```

### 1.0.28
* 添加 管理后台“强制派发”和“强制删除”功能
* 优化 快照保存的速度
* 减少 快照保存时的内存使用
* sokcet.d 升为 2.3.4

### 1.0.27
* sokcet.d 升为 2.3.0

### 1.0.26
* 添加 folkmq-broker 对 ws 协议的支持
* sokcet.d 升为 2.2.2

### 1.0.25
* 添加 消息者列表弹窗（在消息看板界面）
* 调整 访问改为单账号模式（使用 ak,sk 名字配置简单些）
* sokcet.d 升为 2.2.0

### 1.0.24
* 优化 连接器的资源回收处理
* 减少 IO层的线程数使用

### 1.0.23
* 修复 scheduled 特殊情况下引发派发慢的问题

### 1.0.22
* 完善 集群可用性（只要有一个节点即可用）
* 优化 节点断链灵敏度（毫秒级）

### 1.0.21
* 添加 broker 集群热扩展机制
* 添加 mq.event.join 新指令（用于加入集群时，同步订阅）
* 添加 client 批量订阅支持（连接前订阅，即为批量） 
* 添加 client::unpublishAsync 取消发布异步模式
* 调整 各端打开时的日志打印
* 调整 ServiceListener 打开时的日志打印

### 1.0.20
* 添加 folkmq-server 控制台消息发布功能
* 添加 folkmq-broker 控制台消息发布功能
* 添加 MqClient 取消发布的方法
* 添加 后端服务启动打印信息
* 添加 队列会话去重处理
* sokcet.d 升为 2.1.12

### 1.0.19
* 添加 folkmq-server 最大消息等待时间配置（默认 180s，之前为2h）！！！
* 优化 folkmq-server 快照持久化可用性，增加“临时文件”与“备份文件”概念！！！
* 优化 folkmq-broker 客户端异常断连的恢复速度与性能！！！
* 调整 folkmq-broker 控制台，会话看板合并到消息看板
* 调整 集群模式下消费者的订阅名字（由 consumerGroup 改为 topic#consumerGroup）！！！
* 调整 服务端派发改为单线程模式（更适合很多队列的情况）！！！
* 调整 客户端分片大小配置（之前为默认16m，现改为1m）
* 取消 发布重试功能（如有需要用户自行处理）

### 1.0.18
* 调整 将“消费者”概念调整为“消费者组”概念
* 调整 授权检测时提交版本信息
* 调整 客户端创建接口，更友好些
* 修复 broker 集群模式下，folkmq-server 无法触发启动与停目快照事件的问题
* 添加 控制台官网超链接
* 添加 folkmq-broker 给节点推送“更新快照”指令的功能
* 优化 folkmq-broker 对没有消费会话时的派发处理（自动转为ack失败）
* 优化 folkmq-server 快照加载机制
* 优化 topic 订阅关系快照格式（兼容旧版）
* 优化 消息派发处理细节
* 优化 答复流程（避免对方已关停，还在不断答复）

### 1.0.17
* 添加 folkmq-broker 授权检测
* 添加 folkmq-broker 主题看板
* 添加 folkmq-broker 队列看板（从 folkmq-server 定时汇总统计数据）
* 添加 folkmq-broker 节点看板
* 优化 folkmq-server 对时序的处理（仅对不同 ms 有序）
* 简化 folkmq-server 连接 folkmq-broker 的地址，不需要加 @=folkmq-server （自动处理了）
* socket.d 升为 2.1.6

### 1.0.15
* 调整 folkmq-broker 启用鉴权后，对所有连接做鉴权（之前 folkmq-server 是跳过的，不安全）
* 修复 folkmq-server 在集群模式下没有触发停止快照的问题
* 添加 folkmq-server 对 multi-broker 集群模式支持
* 添加 folkmq-client 对 multi-server 地址支持（也对 multi-broker 支持）

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