## FolkMQ

* 基于 [Socket.D 通讯应用协议](https://gitee.com/noear/socketd) 开发的消息队列。支持订阅和发布功能，俗称：民谣消息队列（FolkMQ）

## 开发过程视频

* [[Socket.D 实战] 之录播手写 FolkMQ (1)](https://www.bilibili.com/video/BV1dj411j7PQ/)
  * 完成客户端功能实现
* [[Socket.D 实战] 之录播手写 FolkMQ (2)](https://www.bilibili.com/video/BV1EC4y177sb/)
  * 完成服务端功能实现
  * 完成通讯测试
* [[Socket.D 实战] 之录播手写 FolkMQ (3)](https://www.bilibili.com/video/BV11v411c7kJ/)
  * 添加专用连接地址
  * 添加异步订阅与发布
  * 添加AK/SK鉴权
* [[Socket.D 实战] 之录播手写 FolkMQ (4)](https://www.bilibili.com/video/BV1oc41167DY/)
  * 添加订阅身份支持（支持：以实例订阅，以集群订阅）
* [[Socket.D 实战] 之录播手写 FolkMQ (5)](https://www.bilibili.com/video/BV1zc41167Uj/)
  * 添加客户端消费的ACK机制支持
* [[Socket.D 实战] 之录播手写 FolkMQ (6)](https://www.bilibili.com/video/BV1pe411f7BX/)
  * 添加用户身份队列化
  * 添加消息派发时序化
* [[Socket.D 实战] 之录播手写 FolkMQ (7)](https://www.bilibili.com/video/BV1iM411Z7cu/)
  * 添加服务端的ACK机制支持
  * 添加消息重试机制
* [Socket.D 实战] 之录播手写 FolkMQ (8), 预告
  * 优化代码
  * 解决客户端可能因为网络原因没法ack的问题
  * 添加持久化和高可用支持
  * 添加集群支持

## 成果示例

* server

```java
public class ServerDemo {
    public static void main(String[] args) throws Exception {
        MqServer server = new MqServerImpl()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .start(9393);
    }
}
```

* client

```java
public class ClientDemo1 {
    public static void main(String[] args) throws Exception {
        //客户端
        MqClient client = new MqClientImpl(
                "folkmq://127.0.0.1:9393?accessKey=folkmq&accessSecretKey=YapLHTx19RlsEE16");

        //订阅
        client.subscribe("demo", ((topic, message) -> {
            System.out.println("ClientDemo1::" + topic + " - " + message);
        }));

        //发布
        client.publish("demo", "hi");

        for (int i = 0; i < 10; i++) {
            Thread.sleep(100);
            client.publish("demo", "hi");
        }
    }
}
```
