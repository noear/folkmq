

## 一、服务端容器镜像

| 镜像                            | 说明                        |
|-------------------------------|---------------------------|
| noearorg/folkmq-server:1.0.10 | 服务端（管理端口：8602，消息端口：18602） |
| noearorg/folkmq-broker:1.0.10 | 服务端（管理端口：8602，消息端口：18602） |

当使用 broker 集群时，把 folkmq-server 端口改成：8601


| 端口    | 说明         |
|-------|------------|
| 8602  | 管理端口（即主端口） |
| 18602 | 消息服务端口     |


## 二、单例部署说明

### 1、docker 部署方式

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-server:1.0.10 
```

### 2、docker-compose 部署方式

下载 `docker-compose.yml` 文件，通过 `docker-compose up` 启动服务


### 3、jar 部署方式

编译源码提到 `folkmq-server/target/folkmq-server.jar`，运行指令：

```
java -jar folkmq-server.jar
```

## 三、集群部署说明

### 1、启动 broker

使用 8602 端口（方便让客户端形成习惯）

```
docker run -p 18602:18602 -p 8602:8602 noearorg/folkmq-broker:1.0.10 
```

### 2、启动 server（多个）

通过 folkmq.broker 变量，指向 broker 服务

```
docker run -p 18611:18602 -p 8611:8602 -e folkmq.broker="sd:tcp://192.168.3.2:18602?@=folkmq-server" noearorg/folkmq-server:1.0.10 
docker run -p 18612:18602 -p 8612:8602 -e folkmq.broker="sd:tcp://192.168.3.2:18602?@=folkmq-server" noearorg/folkmq-server:1.0.10 
```

### 3、客户端连接 broker 即可

体验上，还是原来的端口和交互方式。没变化
