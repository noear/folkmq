
# 部署说明

## 一、服务端容器镜像

| 镜像                            | 说明                       |
|-------------------------------|--------------------------|
| noearorg/folkmq-server:1.0.11 | 服务端（主端口：8602，消息端口：18602） |
| noearorg/folkmq-broker:1.0.11 | 代理端（主端口：8602，消息端口：18602） |

当使用 broker 集群时，可以把 folkmq-server 端口改成别的


| 端口    | 说明       |
|-------|----------|
| 8602  | 主端口（即管理端口） |
| 18602 | 消息服务端口   |


## 二、单机部署说明


### 1、docker-compose 部署方式

下载 `docker-compose-standalone.yml`，运行命令：

```
docker-compose -f docker-compose-standalone.yml up
```

### 2、docker 部署方式

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-server:1.0.11 
```



### 3、jar 部署方式

编译源码后，提取 `folkmq-server/target/folkmq-server.jar`，运行命令：

```
java -jar folkmq-server.jar
```

## 三、集群部署说明


### 1、docker-compose 部署方式

下载 `docker-compose-cluster.yml`，运行命令：

```
docker-compose -f docker-compose-cluster.yml up
```

通过 `docker-compose-cluster.yml`，我们可以了解集群部署时 `folkmq-server` 与 `folkmq-broker` 的关系如下：

<img src="DEV-BROKER.png" width="400">