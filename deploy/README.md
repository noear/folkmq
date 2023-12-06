
# 部署说明

## 一、容器镜像

* 容器镜像：（使用 broker 集群模式时，folkmq-server 实例的消息端口会自动关掉）

| 镜像                            | 说明                             |
|-------------------------------|--------------------------------|
| noearorg/folkmq-server:1.0.16 | 服务端（主端口：8602，消息端口：18602），可独立使用 |
| noearorg/folkmq-broker:1.0.16 | 代理端（主端口：8602，消息端口：18602）       |


* 可选配置：

| 属性或环境变量                       | 默认值 |                       |
|-------------------------------|-----|-----------------------|
| `server.port`                 |  8602   | 主端口(http，管理用)         |
|                               |  18602   | 消息端口(tcp)，等于主端口+10000 |
| `folkmq.admin`                |  admin   | 管理密码                  |


* 支持在主端口上提供 prometheus 监控支持，配置提示：

```yml
scrape_configs:
  - job_name: 'folkmq-server'
    scrape_interval: 5s
    metrics_path: '/metrics/prometheus'
    static_configs:
      - targets: ['127.0.0.1:8602']
        labels:
           instance: 'folkmq-server1'
```

## 二、添加消息访问账号：

添属性或环境变量，例： `folkmq.access.ak1=sk1`，`folkmq.access.ak2=sk2`

* 单机部署，在 server 上配置
* 集群部署，在 Broker 上配置


## 三、单机部署说明


### 1、docker-compose 部署方式

下载 `docker-compose-standalone.yml`，运行命令：

```
docker-compose -f docker-compose-standalone.yml up
```

### 2、docker 部署方式

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-server:1.0.16 
```



### 3、jar 部署方式

编译源码后，提取 `folkmq-server/target/folkmq-server.jar`，运行命令：

```
java -jar folkmq-server.jar
```

## 四、Broker 模式集群部署说明


### 1、docker-compose 部署方式

下载 `docker-compose-cluster.yml`，运行命令：

```
docker-compose -f docker-compose-cluster.yml up
```

### 2、集群架构

FolkMQ 是使用 Socket.D 开发的，集群即为 Socket.D Broker 模式集群。详见：[《Socket.D 集群模式》](https://socketd.noear.org/article/737)


## 五、Multi-Broker 模式集群部署说明

这个比较复杂，所以用 jar 形式来表达部署关系。顺带把访问账号也带上

* 启动两个 broker 服务

```
java -Dserver.port=8601 -Dfolkmq.access.ak1=sk1 -jar folkmq-broker.jar
java -Dserver.port=8602 -Dfolkmq.access.ak1=sk1 -jar folkmq-broker.jar
```

* 启动三个 server 服务（连接时多个地址用","隔开）

```
java -Dserver.port=8101 -Dfolkmq.broker='folkmq://127.0.0.1:18601?@=folkmq-server&ak=ak1&sk=sk1,folkmq://127.0.0.1:18602?@=folkmq-server&ak=ak1&sk=sk1' -jar folkmq-server.jar
java -Dserver.port=8102 -Dfolkmq.broker='folkmq://127.0.0.1:18601?@=folkmq-server&ak=ak1&sk=sk1,folkmq://127.0.0.1:18602?@=folkmq-server&ak=ak1&sk=sk1' -jar folkmq-server.jar
java -Dserver.port=8103 -Dfolkmq.broker='folkmq://127.0.0.1:18601?@=folkmq-server&ak=ak1&sk=sk1,folkmq://127.0.0.1:18602?@=folkmq-server&ak=ak1&sk=sk1' -jar folkmq-server.jar
```

* client 示例（连接时多个地址用","隔开）

```java
//客户端
MqClient client = FolkMQ.createClient("folkmq://127.0.0.1:18601?ak=ak1&sk=sk1,folkmq://127.0.0.1:18602?ak=ak1&sk=sk1")
        .connect();
```