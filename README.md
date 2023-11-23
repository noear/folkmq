<h1 align="center" style="text-align:center;">
  FolkMQ
</h1>
<p align="center">
	<strong>一个新起的内存型消息队列（大约 20000 TPS）</strong>
</p>

<p align="center">
    <a target="_blank" href="https://search.maven.org/artifact/org.noear/folkmq">
        <img src="https://img.shields.io/maven-central/v/org.noear/folkmq.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0.txt">
		<img src="https://img.shields.io/:license-Apache2-blue.svg" alt="Apache 2" />
	</a>
   <a target="_blank" href="https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html">
		<img src="https://img.shields.io/badge/JDK-8-green.svg" alt="jdk-8" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-11-green.svg" alt="jdk-11" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-17-green.svg" alt="jdk-17" />
	</a>
    <a target="_blank" href="https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html">
		<img src="https://img.shields.io/badge/JDK-21-green.svg" alt="jdk-21" />
	</a>
    <br />
    <a target="_blank" href='https://gitee.com/noear/folkmq/stargazers'>
        <img src='https://gitee.com/noear/folkmq/badge/star.svg' alt='gitee star'/>
    </a>
    <a target="_blank" href='https://github.com/noear/folkmq/stargazers'>
        <img src="https://img.shields.io/github/stars/noear/folkmq.svg?logo=github" alt="github star"/>
    </a>
</p>

<br/>
<p align="center">
	<a href="https://jq.qq.com/?_wv=1027&k=kjB5JNiC">
	<img src="https://img.shields.io/badge/QQ交流群-870505482-orange"/></a>
</p>


<hr />

## 简介

* 基于 [Socket.D 通讯应用协议](https://gitee.com/noear/socketd) 开发的内存型消息队列。俗称：民谣消息队列（FolkMQ）
* 支持 发布、订阅、定时、ACK，重试、延时
* 没有 集群和持久化
* 大约 25000 TPS（使用 MacBook pro 2020 款本机测试）

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
* [[Socket.D 实战] 之录播手写 FolkMQ (8)](https://www.bilibili.com/video/BV1j34y1w7x2/)
  * 完善ACK要制，确保客户端没有答复还能再发
  * 取消会话查找
* [Socket.D 实战] 之录播手写 FolkMQ (9), 预告
  * 优化代码，
  * 完成压测 100万 收发不丢消息
  * 组织测试用例，构建单测

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
