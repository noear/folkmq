<h1 align="center" style="text-align:center;">
  FolkMQ
</h1>
<p align="center">
	<strong>An in-memory messaging middleware (support for snapshot persistence, Broker clustering)</strong>
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

* Run in Memory + snapshot persistence + Broker cluster mode (optional)
* Can add ws, UDP-based communication (and, perhaps, iot)

| role     | function                                                                                    | 
|----------|---------------------------------------------------------------------------------------------|
| Producer | Publish messages (Qos0, Qos1), publish timed messages (Qos0, Qos1), publish retry           |     
|          |                                                                                             |       
| Consumer | Subscribe, unsubscribe                                                                      |        
| Consumer | Consume-ACK                                                                                 |      
|          |                                                                                             |     
| Server   | Publish-Confirm, Subscribe-Confirm, Unsubscribe-Confirm, Distribute-Retry, Distribute-Delay | 
| Server   | In-memory running, snapshot persistence (automatic, offline, manual)                        |           


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
* Presentation of results：[《[FolkMQ] 一个新的内存型消息队列（快，特别的快）》](https://www.bilibili.com/video/BV1mc411D7pY/)
* Quick start：[《FolkMQ - Helloworld 入门》](https://www.bilibili.com/video/BV1Yj411L7fB/)

## Helloworld

### 1、Start the server (more deployment references: [deployment Notes](deploy))

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-server:1.0.13 
```

### 2、Write client-side code

* maven import

```xml
<dependencies>
    <!-- Optional packages: java-tcp (~ 90kb), smartsocket (~ 260Kb), netty (~ 2.5Mb) -->
    <dependency>
        <groupId>org.noear</groupId>
        <artifactId>folkmq-transport-java-tcp</artifactId>
        <version>1.0.13</version>
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
        client.publish("demo", new MqMessage("helloworld!")).get();
    }
}
```

## Consume automatic retry and delay policies

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


## Dictionary of main client interfaces

See：[《API.md》](API.md)

## Architecture diagram

<img src="folkmq_schema.png" width="500" />



