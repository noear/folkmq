
# 部署说明

## 一、服务端容器镜像

| 镜像                            | 说明                       |
|-------------------------------|--------------------------|
| noearorg/folkmq-server:1.0.10 | 服务端（主端口：8602，消息端口：18602） |
| noearorg/folkmq-broker:1.0.10 | 代理端（主端口：8602，消息端口：18602） |

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

### 2、docker 部署方式（一般用于临时试用）

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-server:1.0.10 
```



### 3、jar 部署方式

编译源码后，提取 `folkmq-server/target/folkmq-server.jar`，运行命令：

```
java -jar folkmq-server.jar
```

## 三、集群部署说明（仍在测试用）


### 1、docker-compose 部署方式

下载 `docker-compose-cluster.yml`，运行命令：

```
docker-compose -f docker-compose-cluster.yml up
```
