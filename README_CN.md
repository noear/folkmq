<h1 align="center" style="text-align:center;">
  FolkMQ
</h1>
<p align="center">
	<strong>一个新的消息中间件（支持快照持久化、Broker 集群）</strong>
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

##### 语言： 中文 | [English](README.md)

<hr />

## 简介

* 采用 “单线程” + “多路复用” + "内存运行" + "快照持久化" + "Broker 集群模式"（可选）
* 基于 [Socket.D 网络应用协议](https://socketd.noear.org/) 开发。全新设计，自主架构！

| 角色  | 功能                                                     | 
|-----|--------------------------------------------------------|
| 生产端 | 发布消息、定时消息、顺序消息、可过期消息、事务消息。发送请求（rpc）。支持 Qos0、Qos1       |
|     |                                                        |  
| 消费端 | 订阅、取消订阅。消费-ACK（自动、手动）。监听（rpc）                               |    
|     |                                                        |    
| 服务端 | 发布-Confirm、订阅-Confirm、取消订阅-Confirm、派发-Retry、派发-Delayed | 
| 服务端 | 单线程、内存运行、快照持久化（自动、停机、手动）、Broker 模式集群、集群热扩展             |   



## 软件使用授权

FolkMQ 为商业开源软件，其中：

* 单机部署版，为社区版功能（免费授权使用）
* 集群部署版，为专业版、企业版功能（付费授权使用）。详见：[《版本规划》](https://folkmq.noear.org/article/edition)


## 特点


* 高吞吐量、低延迟

FolkMQ 纯内存运行，每秒能处理几十万条消息，最低延迟不到1毫秒。

* 可扩展性

FolkMQ Broker 集群<mark>支持 folkmq-server 节点热扩展</mark>。流量高时随时加，流量低时可减

* 持久性、可靠性

消息被快照持久化（类似于 redis）到本地磁盘，并且支持数据备份防止数据丢失


* 快（单机版，大约 180K TPS）。有点像 Redis 之于 MySql。

<img src="DEV-TEST.png" width="600" />

//使用 MacBook pro 2020 + JDK8 本机测试，单客户端发与收（跑分难免有波动，我是选了好看点的）

### 加入到社区交流群

| QQ交流群：316697724                       | 微信交流群（申请时输入：FolkMQ）          |
|---------------------------|----------------------------------------|
|        | <img src="group_wx.png" width="120" /> 



## 开发过程视频记录

* 开发过程视频：[《DEV-RECORD.md》](DEV-RECORD.md)
* 成果展示：[《[FolkMQ] 一个新的内存型消息队列（快，特别的快）》](https://www.bilibili.com/video/BV1mc411D7pY/)
* 快速入门：[《FolkMQ - Helloworld 入门》](https://www.bilibili.com/video/BV1Yj411L7fB/)

### 官网

https://folkmq.noear.org

### 特别感谢JetBrains对开源项目支持

<a href="https://jb.gg/OpenSourceSupport">
  <img src="https://user-images.githubusercontent.com/8643542/160519107-199319dc-e1cf-4079-94b7-01b6b8d23aa6.png" align="left" height="100" width="100"  alt="JetBrains">
</a>

