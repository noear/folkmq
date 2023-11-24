
### 1.0.0-M2

* identity 概念改为 user 概念
* 发消息时添加 scheduled 属性，支持发定时消息
* 将消费者和订阅者接口合到 MqClient，减少文件
* Subscription 更名 MqSubscription
* 简化派发方式