<h1 align="center" style="text-align:center;">
  FolkMQ
</h1>
<p align="center">
	<strong>作一个体验最简单的消息中间件。更简单，更未来！</strong>
</p>

<p align="center">
	<a href="https://folkmq.noear.org/">https://folkmq.noear.org</a>
</p>


<p align="center">
    <a target="_blank" href="https://search.maven.org/artifact/org.noear/folkmq">
        <img src="https://img.shields.io/maven-central/v/org.noear/folkmq.svg?label=Latest-Version" alt="Latest-Version" />
    </a>
    <a target="_blank" href="LICENSE">
		<img src="https://img.shields.io/:license-AGPL3.0-blue.svg" alt="AGPL3.0" />
	</a>
    <a>
		<img src="https://img.shields.io/badge/Java-8~22-green.svg" alt="Java-8~22" />
	</a>
    <a>
		<img src="https://img.shields.io/badge/Kotlin-8+-green.svg" alt="Kotlin-8+" />
	</a>
    <a>
		<img src="https://img.shields.io/badge/JavaScript-es6+-green.svg" alt="JavaScript-es6+" />
	</a>
    <a>
		<img src="https://img.shields.io/badge/Python-3.10+-green.svg" alt="Python-3.10+" />
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

## 更简单，才更未来

源码简单！架构简单！部署简单！使用简单！（可内嵌、可单机、可集群）

## 授权说明

本项目采用 AGPL 开源协议，禁止二次封装开源。获得授权后才可使用，其中：

* 社区版本（免费）采用登记授权方式：[登记入口](https://gitee.com/noear/folkmq/issues/I9L2CL)
* 企业版本采用付费授权方式

## 功能简介

| 角色  | 功能                                                     | 
|-----|--------------------------------------------------------|
| 生产端 | 发布普通消息、Qos0消息、定时消息、顺序消息、广播消息、可过期消息、事务消息       |
|     |                                                        |  
| 消费端 | 订阅、取消订阅。消费-ACK（自动、手动）。监听（rpc）                          |    
|     |                                                        |    
| 服务端 | 发布-Confirm、订阅-Confirm、取消订阅-Confirm、派发-Retry、派发-Delayed | 
| 服务端 | 单线程架构、支持快照持久化（自动、停机、手动）、Broker 模式集群、集群热扩展              |   


## 特点


* 高吞吐量、低延迟

集群模式每秒能处理百万消息，最低延迟不到1毫秒。

* 可扩展性

集群模式<mark>支持服务节点热扩展</mark>。流量高时随时加，流量低时可减

* 持久性、可靠性

消息被快照持久化（类似于 redis）到本地磁盘，并且支持数据备份防止数据丢失


* 快（单机版，大约 180K TPS）

<img src="DEV-TEST.png" width="600" />

//使用 MacBook pro 2020 + JDK8 本机测试，单客户端发与收（跑分难免有波动，我是选了好看点的）

### 加入到社区交流群

| QQ交流群：316697724                       | 微信交流群（申请时输入：FolkMQ）          |
|---------------------------|----------------------------------------|
|        | <img src="group_wx.png" width="120" /> 



## 开发过程视频记录

* 开发过程视频：[《DEV-RECORD.md》](DEV-RECORD.md)
* 快速入门：[《FolkMQ - Helloworld 入门》](https://www.bilibili.com/video/BV1Yj411L7fB/)

### 官网

https://folkmq.noear.org

### 特别感谢JetBrains对开源项目支持

<a href="https://jb.gg/OpenSourceSupport">
  <img src="https://user-images.githubusercontent.com/8643542/160519107-199319dc-e1cf-4079-94b7-01b6b8d23aa6.png" align="left" height="100" width="100"  alt="JetBrains">
</a>

