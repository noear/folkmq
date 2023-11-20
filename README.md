## FolkMQ

* 基于 Socket.D 开发的，时实消息队列。支持订阅和发布功能，俗称：民谣消息队列（FolkMQ）

## 开发过程视频

* [[Socket.D 实战] 之录播手写 FolkMQ (1)](https://www.bilibili.com/video/BV1dj411j7PQ/)
* [[Socket.D 实战] 之录播手写 FolkMQ (2)](https://www.bilibili.com/video/BV1EC4y177sb/)
* [[Socket.D 实战] 之录播手写 FolkMQ (3)](https://www.bilibili.com/video/BV11v411c7kJ/)

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
