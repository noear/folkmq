<h1 align="center" style="text-align:center;">
  FolkMQ
</h1>
<p align="center">
	<strong>一个内存型消息中间件（支持快照持久化、Broker 集群）</strong>
</p>

<p align="center">
    <a target="_blank" href="https://search.maven.org/artifact/org.noear/folkmq">
        <img src="https://img.shields.io/maven-central/v/org.noear/folkmq.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="LICENSE">
		<img src="https://img.shields.io/:license-LGPL2.1-blue.svg" alt="Apache 2" />
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

##### 语言： 中文 | [English](README.md)

<hr />

## 简介

* 采用 "内存运行" + "快照持久化" + "Broker 集群模式"（可选）
* 基于 [Socket.D 开发](https://socketd.noear.org/)（它有的特性全支持），尤其是单连接“多路复用”！也可增加 udp 之类的协议支持

| 角色  | 功能                                                     | 
|-----|--------------------------------------------------------|
| 生产端 | 发布消息（Qos0、Qos1）、发布定时消息（Qos0、Qos1）、发布重试                 |
|     |                                                        |  
| 消费端 | 订阅、取消订阅                                                |   
| 消费端 | 消费-ACK（自动、手动）                                          |      
|     |                                                        |    
| 服务端 | 发布-Confirm、订阅-Confirm、取消订阅-Confirm、派发-Retry、派发-Delayed | 
| 服务端 | 内存运行、快照持久化（自动、停机、手动）                                   |   


## 特点

* 快、是真的快（大约 100_000 TPS）。有点像 Redis 之于 MySql。

<img src="DEV-TEST.png" width="600" />

//使用 MacBook pro 2020 + JDK8 本机测试，单客户端发与收（跑分难免有波动，我是选了好看点的）

* 简单的管理后台

<img src="DEV-PREVIEW.png" width="600" />

### 加入到社区交流群

| QQ交流群：316697724                       | 微信交流群（申请时输入：FolkMQ）          |
|---------------------------|----------------------------------------|
|        | <img src="group_wx.png" width="120" /> 



## 开发过程视频记录

* 开发过程视频：[《DEV-RECORD.md》](DEV-RECORD.md)
* 成果展示：[《[FolkMQ] 一个新的内存型消息队列（快，特别的快）》](https://www.bilibili.com/video/BV1mc411D7pY/)
* 快速入门：[《FolkMQ - Helloworld 入门》](https://www.bilibili.com/video/BV1Yj411L7fB/)

## 你好世界！

### 1、启动服务 (更多部署参考：[部署说明](deploy))

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-server:1.0.14 
```

### 2、编写客户端代码

* maven import

```xml
<dependencies>
    <!-- 可选包：java-tcp（90kb左右）, smartsocket（260Kb左右）, netty（2.5Mb左右） -->
    <dependency>
        <groupId>org.noear</groupId>
        <artifactId>folkmq-transport-java-tcp</artifactId>
        <version>1.0.14</version>
    </dependency>
</dependencies>
```


* client(consumer + producer) use

```java
public class ClientDemo1 {
    public static void main(String[] args) throws Exception {
        //客户端（鉴权为可选。服务端，不添加则不鉴权）
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        //订阅（consumer = ip or cluster name）
        client.subscribe("demo", "demoapp", message -> {
            System.out.println(message);
        });

        //发布
        client.publish("demo", new MqMessage("helloworld!"));
    }
}
```

## 消费自动重试与延时策略

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
| n..  | 2h   | 第8次之后都是2小时 |


## 客户端主要接口字典

详见：[《API.md》](API.md)

## 架构图

<img src="folkmq_schema.png" width="600" />
