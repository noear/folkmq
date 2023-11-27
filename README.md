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
* 支持 快照持久化（类似 redis 的策略）
* 功能 订阅、取消订阅、发布消息、发布定时消息、ACK，重试、延时、Qos0、Qos1
* 没有 集群功能（用户可以自建）

## 特点

* 快、是真的特别快（大约 100_000 TPS）。有点像 redis 之于 mysql。

<img src="DEV-TEST.png" width="600" />

//使用 MacBook pro 2020 + JDK8 本机测试，单客户端发与收（跑分难免有波动，我是选了好看点的）

* 简单的管理后台

<img src="DEV-PREVIEW.png" width="600" />

### 加入到社区交流群

| QQ交流群：870505482                       | 微信交流群（申请时输入：SocketD 或 FolkMQ）          |
|---------------------------|----------------------------------------|
|        | <img src="group_wx.png" width="120" /> 

交流群里，会提供 "保姆级" 支持和帮助。如有需要，也可提供技术培训和顾问服务


## 开发过程视频记录

* 开发过程视频： [《DEV-RECORD.md》](DEV-RECORD.md)
* 成果演示：[《[FolkMQ] 一个新的内存型消息队列（快，特别的快）》](https://www.bilibili.com/video/BV1mc411D7pY/)

## 服务端容器镜像

| 镜像                           | 说明                        |
|------------------------------|---------------------------|
| noearorg/folkmq-server:1.0.8 | 服务端（管理端口：8602，消息端口：18602） |

* 运行示例

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-server:1.0.8 
```

* 可选配置

| 属性或环境变量                       | 默认值 |                      |
|-------------------------------|-----|----------------------|
| `server.port`                 |  8602   | 管理端口(http)           |
|                               |  18602   | 消息端口(tcp)，管理端口+10000 |
| `folkmq.admin`                |  admin   | 管理密码                 |

* 消息访问账号：

添属性或环境变量，例： `folkmq.access.ak1=sk1`，`folkmq.access.ak2=sk2`

## 代码示例


* maven

```xml
<dependencies>
    <!-- 可选包：java-tcp（90kb左右）, smartsocket（260Kb左右）, netty（2.5Mb左右） -->
    <dependency>
        <groupId>org.noear</groupId>
        <artifactId>folkmq-transport-java-tcp</artifactId>
        <version>1.0.8</version>
    </dependency>
</dependencies>
```


* server(broker) custom demo

建议直接使用 folkmq-server.jar 或者 folkmq-server 容器镜像

```java
public class ServerDemo {
    public static void main(String[] args) throws Exception {
        //服务端（鉴权为可选。不添加则不鉴权）
        MqServer server = FolkMQ.createServer()
                .addAccess("folkmq", "YapLHTx19RlsEE16")
                .watcher(new MqWatcherSnapshot()) //快照持久化需要添加 folkmq-pro 包
                .start(13602);

        //添加定时快照
        RunUtils.delayAndRepeat(server::save, 1000 * 30);

        //添加关机勾子
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
    }
}
```

* client(consumer + producer) use  demo

```java
public class ClientDemo1 {
    public static void main(String[] args) throws Exception {
        //客户端（鉴权为可选。服务端，不添加则不鉴权）
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:13602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        //订阅
        client.subscribe("demo", "(ip or cluster-name)", message -> {
            System.out.println("ClientDemo1::" + message);
        });

        //::Qos1
        //发布（异步）
        client.publish("demo", "hi");
        //发布（异步），并指定5秒后派发
        client.publish("demo", "hi", new Date(System.currentTimeMillis() + 5000));
        //发布（同步），可以确保发送顺序与服务端有效确认
        client.publish("demo", "hi").get(); //或 .get(1,TimeUnit.SECONDS)，限定超时
        
        //::Qos0
        //发布（异步）
        client.publish("demo", "hi", 0);
        //发布（异步），并指定5秒后派发
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


