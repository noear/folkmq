<h1 align="center" style="text-align:center;">
  FolkMQ
</h1>
<p align="center">
	<strong>A new in-memory messaging middleware (with snapshot persistence)</strong>
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

## Introduction

* Adopt a Redis-like strategy (run in memory + snapshot persistence)
* Features subscribe, unsubscribe, publish messages, publish timed messages, ACK, retry, Delay, Qos0, Qos1
* No clustering yet (you can build your own)

## Features

* Fast, really fast (~ 100_000 TPS). A bit like Redis is to MySql.

<img src="DEV-TEST.png" width="600" />

//Using MacBook pro 2020 + JDK8 native test, single client sending and receiving (running points inevitably have fluctuations, I chose the better one)

* Simple management

<img src="DEV-PREVIEW.png" width="600" />

### Join a community exchange group

| QQ communication group：316697724                       | Wechat communication group (input: FolkMQ when applying)          |
|---------------------------|----------------------------------------|
|        | <img src="group_wx.png" width="120" /> 



## Video recording of development process

* Development process video： [《DEV-RECORD.md》](DEV-RECORD.md)
* Demonstration of results：[《[FolkMQ] 一个新的内存型消息队列（快，特别的快）》](https://www.bilibili.com/video/BV1mc411D7pY/)

## Server-side container image

| Docker                       | Remarks                      |
|------------------------------|---------------------------|
| noearorg/folkmq-server:1.0.8 | Server (management port: 8602, message port: 18602) |

* Optional configuration

| Properties or environment variables  | Default values | Remarks                                                          |
|--------------------------------------|-----|------------------------------------------------------------------|
| `server.port`                        |  8602   | management port (http)                                           |
|                                      |  18602   | Message port (tcp), which is equal to administrative port +10000 |
| `folkmq.admin`                       |  admin   | management password                                              |

* Add a message access account：

Adding attributes or environment variables, for example： `folkmq.access.ak1=sk1`，`folkmq.access.ak2=sk2`

## Helloworld

### 1、Starting the server

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-server:1.0.8 
```

### 2、Write client-side code

* maven import

```xml
<dependencies>
    <!-- Optional packages: java-tcp (~ 90kb), smartsocket (~ 260Kb), netty (~ 2.5Mb) -->
    <dependency>
        <groupId>org.noear</groupId>
        <artifactId>folkmq-transport-java-tcp</artifactId>
        <version>1.0.8</version>
    </dependency>
</dependencies>
```


* client(consumer + producer) use

```java
public class ClientDemo1 {
    public static void main(String[] args) throws Exception {
        //Client (Authentication is optional. Server-side, do not add, do not authenticate)
        MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18602?ak=folkmq&sk=YapLHTx19RlsEE16")
                .connect();

        //Subscribe
        client.subscribe("demo", "demoapp", message -> {
            System.out.println(message);
        });

        //Publish
        client.publish("demo", "helloworld!").get();
    }
}
```

## Automatic retry and delay policies

| Number of distribution | Automatic delay |            |
|------|------|------------|
| 0    | 0s   | It is equivalent to sending out immediately.     |
| 1    | 5s   |            |
| 2    | 30s  |            |
| 3    | 3m   |            |
| 4    | 9m   |            |
| 5    | 15m  |            |
| 6    | 30m  |            |
| 7    | 1h   |            |
| n..  | 2h   | It's 2 hours after the 8th time |


## Dictionary of client interfaces

```java
//Message client interface
public interface MqClient {
    MqClient connect() throws IOException;

    void disconnect() throws IOException;

    MqClient config(ClientConfigHandler configHandler);

    MqClient autoAcknowledge(boolean auto);

    void subscribe(String topic, String consumer, MqConsumeHandler consumerHandler) throws IOException;

    void unsubscribe(String topic, String consumer) throws IOException;
    
    default CompletableFuture<?> publish(String topic, String content) throws IOException {
        return publish(topic, content, null, 1);
    }
    
    default CompletableFuture<?> publish(String topic, String content, int qos) throws IOException {
        return publish(topic, content, null, qos);
    }
    
    default CompletableFuture<?> publish(String topic, String content, Date scheduled) throws IOException {
        return publish(topic, content, scheduled, 1);
    }
    
    CompletableFuture<?> publish(String topic, String content, Date scheduled, int qos) throws IOException;
}
```


