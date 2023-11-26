<h1 align="center" style="text-align:center;">
  FolkMQ
</h1>
<p align="center">
	<strong>一个新起的内存型消息队列（支持快照持久化）</strong>
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
* 支持 快照持久化
* 功能 订阅、取消订阅、发布消息、发布定时消息、ACK，重试、延时、Qos0、Qos1
* 没有 集群功能（用户可以自建）


## 特点

快、特别的快（大约 100_000 TPS）。基于内存，有点像 redis 之于 mysql。

<img src="DEV-TEST.png" width="600" />

//使用 MacBook pro 2020 + JDK8 本机测试，单客户端发与收

### 加入到社区交流群

| QQ交流群：870505482                       | 微信交流群（申请时输入：SocketD 或 FolkMQ）          |
|---------------------------|----------------------------------------|
|        | <img src="group_wx.png" width="120" /> 

交流群里，会提供 "保姆级" 支持和帮助。如有需要，也可提供技术培训和顾问服务


## 开发过程视频记录

详见： [DEV-RECORD.md](DEV-RECORD.md)

## 示例


* maven

```xml
<dependencies>
  <!-- 主包：（33kb左右） -->
    <dependency>
        <groupId>org.noear</groupId>
        <artifactId>folkmq-pro</artifactId>
        <version>1.0.5</version>
    </dependency>

    <!-- 可选传输包：java-tcp（90kb左右）, smartsocket（260Kb左右）, netty（2.5Mb左右） -->
    <dependency>
        <groupId>org.noear</groupId>
        <artifactId>socketd-transport-java-tcp</artifactId>
        <version>2.0.20</version>
    </dependency>
</dependencies>
```


* server(broker) demo

```java
public class ServerDemo {
    public static void main(String[] args) throws Exception {
        //服务端（鉴权为可选。不添加则不鉴权）
        MqServer server = new MqServerImpl()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .persistent(new MqPersistentSnapshot())
                .start(9393);

        //添加定时快照
        RunUtils.delayAndRepeat(server::save, 1000 * 30);

        //添加关机勾子
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
```

* client(consumer + producer)  demo

```java
public class ClientDemo1 {
    public static void main(String[] args) throws Exception {
        //客户端（鉴权为可选。服务端，不添加则不鉴权）
        MqClient client = new MqClientImpl("folkmq://127.0.0.1:9393?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        //订阅
        client.subscribe("demo", "(ip or cluster-name)", message -> {
            System.out.println("ClientDemo1::" + message);
        });

        //发布(Qos1)
        client.publish("demo", "hi");
        //发布(Qos1)，并指定5秒后派发
        client.publish("demo", "hi", new Date(System.currentTimeMillis() + 5000));
        
        //发布(Qos0) 
        client.publish("demo", "hi", 0);
        //发布(Qos0)，并指定5秒后派发
        client.publish("demo", "hi", new Date(System.currentTimeMillis() + 5000), 0);
    }
}
```

### 自动重试与延时策略

| 派发次数 | 自动延时 |            |
|------|------|------------|
| 0    | 0s   | 相当于马上发     |
| 1    | 5s   |            |
| 2    | 30s  |            |
| 3    | 3m   |            |
| 4    | 9m   |            |
| 5    | 15m  |            |
| 6    | 30m  |            |
| 7    | 1h   |            |
| n..  | 2h   | 第八次之后都是2小时 |


