<h1 align="center" style="text-align:center;">
  FolkMQ
</h1>
<p align="center">
	<strong>A new messaging middleware (supports snapshot persistence, Broker clustering)</strong>
</p>

<p align="center">
	<a href="https://folkmq.noear.org/">https://folkmq.noear.org</a>
</p>


<p align="center">
    <a target="_blank" href="https://search.maven.org/artifact/org.noear/folkmq">
        <img src="https://img.shields.io/maven-central/v/org.noear/folkmq.svg?label=Maven%20Central" alt="Maven" />
    </a>
    <a target="_blank" href="LICENSE">
		<img src="https://img.shields.io/:license-LGPL2.1-blue.svg" alt="LGPL2.1" />
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

##### Language: English | [中文](README_CN.md)
<hr />

## Introduction

* Run in Memory + snapshot persistence + Broker cluster mode (optional)
* based on [Socket. D development] (https://socketd.noear.org/), it features full support), especially the single connection "multiplex"! Protocols such as udp can also be added


* Use "multiplexing" + "running in memory" + "snapshot persistence" + "Broker cluster mode "(optional)
* based on [Socket.D Network Application protocol](https://socketd.noear.org/) Development. New design, independent architecture!


| role     | function                                                                                    | 
|----------|---------------------------------------------------------------------------------------------|
| Producer | Publish messages, timed messages, sequential messages, expireable messages, transactional messages. Send Request (rpc) Qos0 and Qos1 are supported                      |     
|          |                                                                                             |       
| Consumer | Subscribe, unsubscribe. Consumption -ACK (automatic, manual). Listening (rpc)          |      
|          |                                                                                             |     
| Server   | Publish -Confirm, subscribe -Confirm, unsubscribe -Confirm, dispatch -Retry, dispatch -delay | 
| Server   | Single thread, run in memory, snapshot persistence (automatic, down, manual), Broker mode clustering, cluster hot scaling                    |   



## License to Use Software

FolkMQ is a commercial open source software that:

* Standalone deployment, Community Edition features (free for licensed use)
* Cluster deployment edition, for Professional edition, Enterprise edition features (paid license use)。详见：[《Product planning》](https://folkmq.noear.org/article/edition)


## Features



* High throughput, low latency

FolkMQ runs purely in memory and can process hundreds of thousands of messages per second with minimum latency of less than a millisecond.

* Extensibility

FolkMQ Broker cluster <mark> supports FolkMQ-Server node hot extension </mark>. When the flow is high, it can be added at any time, and the flow can be reduced when it is low

* Durability, reliability

Messages are snapshot persisted (similar to redis) to local disk, and data backup is supported to prevent data loss


* Fast (standalone, ~ 180K TPS). A bit like Redis is to MySql.


<img src="DEV-TEST.png" width="600" />

//Using MacBook pro 2020 + JDK8 native test, single client sending and receiving (running points inevitably have fluctuations, I chose the better one)



### Join a community exchange group

| QQ communication group：316697724                       | Wechat communication group (input: FolkMQ when applying)          |
|---------------------------|----------------------------------------|
|        | <img src="group_wx.png" width="120" /> 



## Video recording of development process

* Development process video： [《DEV-RECORD.md》](DEV-RECORD.md)
* Presentation of results：[《[FolkMQ] 一个新的内存型消息队列（快，特别的快）》](https://www.bilibili.com/video/BV1mc411D7pY/)
* Quick start：[《FolkMQ - Helloworld 入门》](https://www.bilibili.com/video/BV1Yj411L7fB/)

### Official website

https://folkmq.noear.org

### Special thanks to JetBrains for supporting the open source project

<a href="https://jb.gg/OpenSourceSupport">
  <img src="https://user-images.githubusercontent.com/8643542/160519107-199319dc-e1cf-4079-94b7-01b6b8d23aa6.png" align="left" height="100" width="100"  alt="JetBrains">
</a>






